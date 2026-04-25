package com.project.controller;

import java.util.List;
import java.util.concurrent.ExecutorService;
import com.project.dao.ZadanieDAO;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ZadanieController {
    @FXML private Button btnPowrot;
    @FXML private TableView<Zadanie> tblZadanie;
    @FXML private TableColumn<Zadanie, String> colNazwa;
    @FXML private TableColumn<Zadanie, String> colOpis;
    @FXML private TableColumn<Zadanie, Integer> colKolejnosc;
    @FXML private TableColumn<Zadanie, Void> colAkcje;

    private ExecutorService wykonawca;
    private ZadanieDAO zadanieDAO;
    private Projekt projekt;
    private ObservableList<Zadanie> zadania;

    public ZadanieController(Projekt projekt, ZadanieDAO zadanieDAO, ExecutorService wykonawca) {
        this.projekt = projekt;
        this.zadanieDAO = zadanieDAO;
        this.wykonawca = wykonawca;
    }

    @FXML
    public void initialize() {
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        colKolejnosc.setCellValueFactory(new PropertyValueFactory<>("kolejnosc"));

        colAkcje.setCellFactory(column -> new TableCell<Zadanie, Void>() {
            private final GridPane pane;
            {
                Button btnEdit = new Button("Edytuj");
                Button btnRemove = new Button("Usuń");
                btnEdit.setOnAction(e -> edytujZadanie(getTableView().getItems().get(getIndex())));
                btnRemove.setOnAction(e -> usunZadanie(getTableView().getItems().get(getIndex())));
                pane = new GridPane();
                pane.setHgap(5);
                pane.add(btnEdit, 0, 0);
                pane.add(btnRemove, 1, 0);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        zadania = FXCollections.observableArrayList();
        tblZadanie.setItems(zadania);
        loadZadania();
    }

    private void loadZadania() {
        wykonawca.execute(() -> {
            try {
                List<Zadanie> list = zadanieDAO.getZadaniaByProjektId(projekt.getProjektId());
                Platform.runLater(() -> {
                    zadania.clear();
                    zadania.addAll(list);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Błąd ładowania zadań", e.getMessage()));
            }
        });
    }

    @FXML
    private void onActionBtnDodaj(ActionEvent event) {
        Zadanie z = new Zadanie();
        z.setProjektId(projekt.getProjektId());
        edytujZadanie(z);
    }

    private void edytujZadanie(Zadanie zadanie) {
        Dialog<Zadanie> dialog = new Dialog<>();
        dialog.setTitle("Zadanie");
        dialog.setHeaderText(zadanie.getZadanieId() == null ? "Dodawanie zadania" : "Edycja zadania");

        TextField txtNazwa = new TextField();
        if(zadanie.getNazwa() != null) txtNazwa.setText(zadanie.getNazwa());

        TextArea txtOpis = new TextArea();
        if(zadanie.getOpis() != null) txtOpis.setText(zadanie.getOpis());

        TextField txtKolejnosc = new TextField();
        if(zadanie.getKolejnosc() != null) txtKolejnosc.setText(zadanie.getKolejnosc().toString());

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.add(new Label("Nazwa:"), 0, 0); grid.add(txtNazwa, 1, 0);
        grid.add(new Label("Opis:"), 0, 1); grid.add(txtOpis, 1, 1);
        grid.add(new Label("Kolejność:"), 0, 2); grid.add(txtKolejnosc, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType btnOk = new ButtonType("Zapisz", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        dialog.setResultConverter(b -> {
            if (b == btnOk) {
                zadanie.setNazwa(txtNazwa.getText());
                zadanie.setOpis(txtOpis.getText());
                try {
                    zadanie.setKolejnosc(Integer.parseInt(txtKolejnosc.getText()));
                } catch(Exception e) { zadanie.setKolejnosc(0); }
                return zadanie;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(res -> {
            wykonawca.execute(() -> {
                try {
                    zadanieDAO.setZadanie(zadanie);
                    loadZadania();
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Błąd zapisu", e.getMessage()));
                }
            });
        });
    }

    private void usunZadanie(Zadanie zadanie) {
        wykonawca.execute(() -> {
            try {
                zadanieDAO.deleteZadanie(zadanie.getZadanieId());
                loadZadania();
            } catch (Exception e) {
                Platform.runLater(() -> showError("Błąd usuwania", e.getMessage()));
            }
        });
    }

    @FXML
    private void onActionBtnPowrot(ActionEvent event) {
        Stage stage = (Stage) btnPowrot.getScene().getWindow();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}