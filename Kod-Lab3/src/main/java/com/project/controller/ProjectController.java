package com.project.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.project.dao.ProjektDAO;
import com.project.dao.ZadanieDAOImpl;
import com.project.model.Projekt;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormater = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExecutorService wykonawca;
    private ProjektDAO projektDAO;
    private ZadanieDAOImpl zadanieDAO;

    private String search4;
    private Integer pageNo;
    private Integer pageSize;
    private int totalRows = 0;

    @FXML private ChoiceBox<Integer> cbPageSizes;
    @FXML private TableView<Projekt> tblProjekt;
    @FXML private TableColumn<Projekt, Integer> colId;
    @FXML private TableColumn<Projekt, String> colNazwa;
    @FXML private TableColumn<Projekt, String> colOpis;
    @FXML private TableColumn<Projekt, LocalDateTime> colDataCzasUtworzenia;
    @FXML private TableColumn<Projekt, LocalDate> colDataOddania;
    @FXML private TextField txtSzukaj;
    @FXML private Button btnDalej;
    @FXML private Button btnWstecz;
    @FXML private Button btnPierwsza;
    @FXML private Button btnOstatnia;
    @FXML private Label lblPageInfo;

    private ObservableList<Projekt> projekty;

    public ProjectController(ProjektDAO projektDAO) {
        this.projektDAO = projektDAO;
        this.zadanieDAO = new ZadanieDAOImpl();
        this.wykonawca = Executors.newFixedThreadPool(1);
    }

    @FXML
    public void initialize() {
        search4 = "";
        pageNo = 0;
        pageSize = 10;

        cbPageSizes.getItems().addAll(5, 10, 20, 50, 100);
        cbPageSizes.setValue(pageSize);
        cbPageSizes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pageSize = newVal;
                pageNo = 0;
                wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
            }
        });

        colId.setCellValueFactory(new PropertyValueFactory<>("projektId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        
        colDataCzasUtworzenia.setCellValueFactory(new PropertyValueFactory<>("dataCzasUtworzenia"));
        colDataCzasUtworzenia.setCellFactory(column -> new TableCell<Projekt, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setText(null);
                else setText(dateTimeFormater.format(item));
            }
        });

        colDataOddania.setCellValueFactory(new PropertyValueFactory<>("dataOddania"));

        TableColumn<Projekt, Void> colEdit = new TableColumn<>("Akcje");
        colEdit.setCellFactory(column -> new TableCell<Projekt, Void>() {
            private final GridPane pane;
            {
                Button btnTask = new Button("Zadania");
                Button btnEdit = new Button("Edycja");
                Button btnRemove = new Button("Usuń");
                
                btnTask.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnRemove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                
                btnTask.setOnAction(e -> openZadanieFrame(getCurrentProjekt()));
                btnEdit.setOnAction(e -> edytujProjekt(getCurrentProjekt()));
                btnRemove.setOnAction(e -> usunProjekt(getCurrentProjekt()));
                
                pane = new GridPane();
                pane.setAlignment(Pos.CENTER);
                pane.setHgap(10);
                pane.setVgap(10);
                pane.setPadding(new Insets(5, 5, 5, 5));
                pane.add(btnTask, 0, 0);
                pane.add(btnEdit, 0, 1);
                pane.add(btnRemove, 0, 2);
            }
            
            private Projekt getCurrentProjekt() {
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tblProjekt.getColumns().add(colEdit);
        
        colId.setMaxWidth(5000);
        colNazwa.setMaxWidth(10000);
        colOpis.setMaxWidth(10000);
        colDataCzasUtworzenia.setMaxWidth(9000);
        colDataOddania.setMaxWidth(7000);
        colEdit.setMaxWidth(7000);

        projekty = FXCollections.observableArrayList();
        tblProjekt.setItems(projekty);

        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    private void loadPage(String search4, Integer pageNo, Integer pageSize) {
        try {
            final List<Projekt> projektList = new ArrayList<>();
            if (search4 != null && !search4.isEmpty()) {
                if (search4.matches("^[0-9]+$")) {
                    Projekt p = projektDAO.getProjekt(Integer.parseInt(search4));
                    if(p != null) projektList.add(p);
                    totalRows = projektList.size();
                } else if (search4.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    LocalDate date = LocalDate.parse(search4, dateFormatter);
                    projektList.addAll(projektDAO.getProjektyWhereDataOddaniaIs(date, pageNo, pageSize));
                    totalRows = projektDAO.getRowsNumberWhereDataOddaniaIs(date);
                } else {
                    projektList.addAll(projektDAO.getProjektyWhereNazwaLike(search4, pageNo, pageSize));
                    totalRows = projektDAO.getRowsNumberWhereNazwaLike(search4);
                }
            } else {
                projektList.addAll(projektDAO.getProjekty(pageNo, pageSize));
                totalRows = projektDAO.getRowsNumber();
            }

            Platform.runLater(() -> {
                projekty.clear();
                projekty.addAll(projektList);
                updatePaginationControls();
            });
        } catch (RuntimeException e) {
            String errMsg = "Błąd podczas pobierania listy projektów.";
            logger.error(errMsg, e);
            Platform.runLater(() -> showError(errMsg, e.getMessage()));
        }
    }

    private void updatePaginationControls() {
        btnWstecz.setDisable(pageNo == 0);
        btnPierwsza.setDisable(pageNo == 0);
        boolean isLastPage = (pageNo + pageSize) >= totalRows;
        btnDalej.setDisable(isLastPage);
        btnOstatnia.setDisable(isLastPage);
        
        int currentPage = (pageNo / pageSize) + 1;
        int maxPage = Math.max(1, (int)Math.ceil((double)totalRows / pageSize));
        if(lblPageInfo != null) lblPageInfo.setText("strona " + currentPage + " z " + maxPage);
    }

    @FXML private void onActionBtnSzukaj(ActionEvent event) {
        search4 = txtSzukaj.getText().trim();
        pageNo = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML private void onActionBtnDalej(ActionEvent event) {
        if ((pageNo + pageSize) < totalRows) {
            pageNo += pageSize;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML private void onActionBtnWstecz(ActionEvent event) {
        if (pageNo > 0) {
            pageNo = Math.max(0, pageNo - pageSize);
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML private void onActionBtnPierwsza(ActionEvent event) {
        pageNo = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML private void onActionBtnOstatnia(ActionEvent event) {
        int maxPage = Math.max(0, (int)Math.ceil((double)totalRows / pageSize) - 1);
        pageNo = maxPage * pageSize;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML public void onActionBtnDodaj(ActionEvent event) {
        edytujProjekt(new Projekt());
    }

    private void edytujProjekt(Projekt projekt) {
        Dialog<Projekt> dialog = new Dialog<>();
        dialog.setTitle("Edycja");
        if (projekt.getProjektId() != null) dialog.setHeaderText("Edycja danych projektu");
        else dialog.setHeaderText("Dodawanie projektu");
        dialog.setResizable(true);

        Label lblId = getRightLabel("Id: ");
        Label lblNazwa = getRightLabel("Nazwa: ");
        Label lblOpis = getRightLabel("Opis: ");
        Label lblDataCzasUtworzenia = getRightLabel("Data utworzenia: ");
        Label lblDataOddania = getRightLabel("Data oddania: ");

        Label txtId = new Label();
        if (projekt.getProjektId() != null) txtId.setText(projekt.getProjektId().toString());

        TextField txtNazwa = new TextField();
        if (projekt.getNazwa() != null) txtNazwa.setText(projekt.getNazwa());

        TextArea txtOpis = new TextArea();
        txtOpis.setPrefRowCount(6);
        txtOpis.setPrefColumnCount(40);
        txtOpis.setWrapText(true);
        if (projekt.getOpis() != null) txtOpis.setText(projekt.getOpis());

        Label txtDataUtworzenia = new Label();
        if (projekt.getDataCzasUtworzenia() != null)
            txtDataUtworzenia.setText(dateTimeFormater.format(projekt.getDataCzasUtworzenia()));

        DatePicker dtDataOddania = new DatePicker();
        dtDataOddania.setPromptText("RRRR-MM-DD");
        dtDataOddania.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate date) {
                return date != null ? dateFormatter.format(date) : null;
            }
            @Override public LocalDate fromString(String text) {
                return (text == null || text.trim().isEmpty()) ? null : LocalDate.parse(text, dateFormatter);
            }
        });
        if (projekt.getDataOddania() != null) dtDataOddania.setValue(projekt.getDataOddania());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(5, 5, 5, 5));
        
        if (projekt.getProjektId() != null) {
            grid.add(lblId, 0, 0); grid.add(txtId, 1, 0);
            grid.add(lblDataCzasUtworzenia, 0, 1); grid.add(txtDataUtworzenia, 1, 1);
        }
        grid.add(lblNazwa, 0, 2); grid.add(txtNazwa, 1, 2);
        grid.add(lblOpis, 0, 3); grid.add(txtOpis, 1, 3);
        grid.add(lblDataOddania, 0, 4); grid.add(dtDataOddania, 1, 4);

        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk = new ButtonType("Zapisz", ButtonData.OK_DONE);
        ButtonType buttonTypeCancel = new ButtonType("Anuluj", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, buttonTypeCancel);

        dialog.setResultConverter(butonType -> {
            if (butonType == buttonTypeOk) {
                projekt.setNazwa(txtNazwa.getText().trim());
                projekt.setOpis(txtOpis.getText().trim());
                projekt.setDataOddania(dtDataOddania.getValue());
                return projekt;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            wykonawca.execute(() -> {
                try {
                    projektDAO.setProjekt(projekt);
                    Platform.runLater(() -> loadPage(search4, pageNo, pageSize));
                } catch (RuntimeException e) {
                    Platform.runLater(() -> showError("Błąd zapisu", e.getMessage()));
                }
            });
        });
    }

    private void usunProjekt(Projekt projekt) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Potwierdzenie");
        alert.setHeaderText("Usuwanie projektu");
        alert.setContentText("Czy na pewno usunąć projekt: " + projekt.getNazwa() + "?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                wykonawca.execute(() -> {
                    try {
                        projektDAO.deleteProjekt(projekt.getProjektId());
                        Platform.runLater(() -> loadPage(search4, pageNo, pageSize));
                    } catch (Exception e) {
                        Platform.runLater(() -> showError("Błąd", e.getMessage()));
                    }
                });
            }
        });
    }

    private Stage openZadanieFrame(Projekt projekt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ZadanieFrame.fxml"));
            loader.setControllerFactory(c -> new ZadanieController(projekt, this.zadanieDAO, this.wykonawca));
            Stage stage = new Stage(StageStyle.DECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Zadania projektu: " + projekt.getNazwa());
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
            return stage;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Label getRightLabel(String text) {
        Label lbl = new Label(text);
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        return lbl;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void shutdown() {
        if (wykonawca != null) {
            wykonawca.shutdown();
            try {
                if (!wykonawca.awaitTermination(5, TimeUnit.SECONDS)) wykonawca.shutdownNow();
            } catch (InterruptedException e) {
                wykonawca.shutdownNow();
            }
        }
    }
}