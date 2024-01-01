import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HFloorItem;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

@ExtensionInfo(
        Title = "GImageInjector",
        Description = "Put backgrounds only in the client :)",
        Version = "1.0.2",
        Author = "Julianty"
)

public class GImageInjector extends ExtensionForm implements Initializable {
    public ListView<ImageView> listView;
    public TextField textImage;
    public Button buttonAdd, buttonErase;
    public Slider sliderOffSetX, sliderOffSetY, sliderOffSetZ;
    public Label labelOffSetX, labelOffSetY, labelOffSetZ;
    public TableView <Product> tableView;
    public TableColumn <Product, String> columnImageUrl;
    public TableColumn <Product, Integer> columnFurnitureId;
    public TableColumn <Product, String> columnOffSetX;
    public TableColumn <Product, String> columnOffSetY;
    public TableColumn <Product, String> columnOffSetZ;
    public ObservableList<Product> dataObservableList = FXCollections.observableArrayList();

    public static final String dir;
    static {
        try {
            dir = new File(GImageInjector.class.getProtectionDomain().
                    getCodeSource().getLocation().toURI().getPath()).getParent(); // + "/cache"
        } catch (URISyntaxException e) { throw new RuntimeException(e); }
    }

    // This is for avoid to do it the manually ...
    // Go for example to https://www.habbo.es/gamedata/furnidata_json/1 then press Ctrl + F, type "ads_background" and copy the id
    private static final HashMap<String, Integer> host_adsBackground = new HashMap<>();
    static {
        host_adsBackground.put("game-es.habbo.com", 3704);
        host_adsBackground.put("game-br.habbo.com", 3755);
        host_adsBackground.put("game-tr.habbo.com", 3770);
        host_adsBackground.put("game-us.habbo.com", 3996);
        host_adsBackground.put("game-de.habbo.com", 3707);
        host_adsBackground.put("game-fi.habbo.com", 9509);
        host_adsBackground.put("game-fr.habbo.com", 3708);
        host_adsBackground.put("game-it.habbo.com", 3821);
        host_adsBackground.put("game-nl.habbo.com", 3715);
        host_adsBackground.put("game-s2.habbo.com", 3787);
    }

    @Override // Importante, sin esto el TableView no funciona
    public void initialize(URL location, ResourceBundle resources) {
        columnImageUrl.setCellValueFactory(new PropertyValueFactory<>("productImageUrl")); // Atributos de la clase Product!
        columnFurnitureId.setCellValueFactory(new PropertyValueFactory<>("productFurniID"));
        columnOffSetX.setCellValueFactory(new PropertyValueFactory<>("productOffSetX"));
        columnOffSetY.setCellValueFactory(new PropertyValueFactory<>("productOffSetY"));
        columnOffSetZ.setCellValueFactory(new PropertyValueFactory<>("productOffSetZ"));
        tableView.setItems(dataObservableList);

        File folder = new File(dir + "/GImageInjector"); // "\\" + GImageInjector.class.getName()
        if (!folder.exists()) {
            if (folder.mkdirs()) System.out.println("Folder successfully created");
            else System.err.println("The folder could not be created.");
        }
        else System.out.println("The folder already exists");

        File file = new File(folder, "url_images.txt");    // System.out.println(file);
        if (file.exists()) {
            new Thread(()->{
                try {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);

                    listView.setDisable(true);
                    for(String line: bufferedReader.lines().toArray(String[]::new)){
                        URL url = new URL(line);    // System.out.println(url);
                        ImageView image = new ImageView(url.toExternalForm());
                        image.setFitHeight(150);    image.setFitWidth(150);

                        mapImageToUrl.put(image, url);
                        listView.getItems().add(image);
                        Platform.runLater(()-> listView.scrollTo(image));
                    }
                    bufferedReader.close(); fileReader.close();
                }
                catch (IOException ignored) {}
                finally {
                    listView.setDisable(false);
                }
            }).start();
        }
        else System.out.println("File doesn't exist: " + file.getAbsolutePath());
    }

    // Look NFT hr-3322-1347.hd-600-8.ch-4025-106-105.lg-4066-107.sh-3089-1425.he-4258.ea-3822-106.cc-3572-1423-106
    public int uniqueId = -1;
    public int currentFurnitureId = -1;
    public String selectedUrlTable;
    public int selectedIdTable;
    public double selectedOffsetXTable, selectedOffsetYTable, selectedOffsetZTable;
    public HashMap<ImageView, URL> mapImageToUrl = new HashMap<>();
    DecimalFormat df = new DecimalFormat("#.00");


    @Override
    protected void onHide() {
        currentFurnitureId = -1; // There are no furni with negative id

        // Loop for delete all images in the client
        for (int j = tableView.getItems().size() - 1; j >= 0; j--) {
            try { Thread.sleep(560); } catch (InterruptedException ignored) {}
            int furni_id = tableView.getItems().get(j).getProductFurniID();
            sendToClient(new HPacket("ObjectRemove", HMessage.Direction.TOCLIENT, String.valueOf(furni_id), false, 12345, 0));
        }
        tableView.getItems().clear();
    }

    // When the extension is installed or connected
    @Override
    protected void initExtension(){
        // Using lambda expresion to detect the host
        onConnect((host, port, APIVersion, versionClient, client) -> {
            if(host_adsBackground.containsKey(host))
                uniqueId = host_adsBackground.get(host);
        });

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            URL url = mapImageToUrl.get(newValue);
            textImage.setText(url.toString());
        });
        sliderOffSetX.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setPosition();
                labelOffSetX.setText("offset X: " + df.format(sliderOffSetX.getValue()));
            }
        });

        /* Useful to use listeners in a boolean
        private BooleanProperty completedProperty = new SimpleBooleanProperty();
        completedProperty.addListener((observable, oldValue, newValue) -> {
            System.out.println("oldValue: " + oldValue);
            System.out.println("newValue: " + newValue);
        });*/

        sliderOffSetY.valueProperty().addListener((observable, oldValue, newValue) -> {
            setPosition();
            labelOffSetY.setText("offset Y: " + df.format(sliderOffSetY.getValue()));
        });

        sliderOffSetZ.valueProperty().addListener((observable, oldValue, newValue) -> {
            setPosition();
            labelOffSetZ.setText("offset Z: " + df.format(sliderOffSetZ.getValue())); //8700 or less in any rooms wtf
        });

        buttonAdd.setOnAction(e -> {
            /*
                Packet Structure:
                {in:ObjectAdd}{i:1}{i:3704}{i:0}{i:0}{i:1}{s:"0.0"}{s:"0.0"}{i:0}{i:1}{i:5}{s:"state"}
                {s:"0"}{s:"offsetZ"}{s:"0.0"}{s:"offsetY"}{s:"0.0"}{s:"imageUrl"}
                {s:"https://cdn.pixabay.com/photo/2016/02/13/12/26/aurora-1197753_960_720.jpg"}
                {s:"offsetX"}{s:"0.0"}{i:-1}{i:1}{i:3614808}{b:false}{b:false}
            */
            currentFurnitureId++;
            sendToClient(new HPacket("ObjectAdd", HMessage.Direction.TOCLIENT, currentFurnitureId, uniqueId, 0, 0
                    , 1, "0.0", "0.0", 0, 1, 5, "state", "0", "offsetZ", String.valueOf(sliderOffSetZ.getValue()),
                    "offsetY", String.valueOf(sliderOffSetY.getValue()), "imageUrl", textImage.getText(), "offsetX",
                    String.valueOf(sliderOffSetX.getValue()), -1, 1, 12345, false, false)); // 1234 its your userid or whatever, dont care
            tableView.getItems().add(new Product(textImage.getText(), currentFurnitureId, sliderOffSetX.getValue(),
                    sliderOffSetY.getValue(), sliderOffSetZ.getValue()));
            tableView.getSelectionModel().selectLast(); // Selecciona el ultimo item de la tabla
        });

        // Runs when an item is added or removed from the tableView
        tableView.getItems().addListener((ListChangeListener.Change<? extends Product> c) -> {
            if(c.next()){ // Avoid exception
                if(c.wasAdded()){
                    try{
                        selectedUrlTable = tableView.getSelectionModel().getSelectedItem().getProductImageUrl();
                        selectedIdTable = tableView.getSelectionModel().getSelectedItem().getProductFurniID();
                        selectedOffsetXTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetX();
                        selectedOffsetYTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetY();
                        selectedOffsetZTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetZ();
                    }catch (Exception ignored){}
                }
            }
        });

        buttonErase.setOnAction(event -> {
            try{
                int furnitureId = tableView.getSelectionModel().getSelectedItem().getProductFurniID();
                tableView.getItems().remove(tableView.getSelectionModel().getSelectedIndex());

                // You get a black screen if you don't delete the furni id in descending order (5, 4, 3...)                         // whatever number
                sendToClient(new HPacket("ObjectRemove", HMessage.Direction.TOCLIENT, String.valueOf(furnitureId), false, 12345, 0));
            } catch (Exception ignored) {}
        });

        // Happens when i give click in a row from the tableView
        tableView.setOnMouseClicked((MouseEvent)->{
            try{
                selectedUrlTable = tableView.getSelectionModel().getSelectedItem().getProductImageUrl();
                selectedIdTable = tableView.getSelectionModel().getSelectedItem().getProductFurniID();
                selectedOffsetXTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetX();

                sliderOffSetX.setValue(selectedOffsetXTable);
                sliderOffSetY.setValue(selectedOffsetYTable);
                sliderOffSetZ.setValue(selectedOffsetZTable);
            }catch (Exception ignored) {}
        });

        // tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {});

        intercept(HMessage.Direction.TOCLIENT, "Objects", hMessage -> {
            try{
                for (HFloorItem hFloorItem: HFloorItem.parse(hMessage.getPacket())){
                    System.out.println(Arrays.toString(hFloorItem.getStuff()));
                }
            }catch (Exception e) { e.printStackTrace(); }
        });
    }

    public void setPosition(){
        try{
            selectedUrlTable = tableView.getSelectionModel().getSelectedItem().getProductImageUrl();
            selectedIdTable = tableView.getSelectionModel().getSelectedItem().getProductFurniID();
            selectedOffsetXTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetX();
            selectedOffsetYTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetY();
            selectedOffsetZTable = tableView.getSelectionModel().getSelectedItem().getProductOffSetZ();

            int indexTable = tableView.getSelectionModel().getSelectedIndex();  // Get the row index
            tableView.getItems().set(indexTable, new Product(selectedUrlTable, selectedIdTable,
                    sliderOffSetX.getValue(), sliderOffSetY.getValue(), sliderOffSetZ.getValue())); // Update the row
            tableView.getSelectionModel().select(indexTable);   // Solve a bug

            sendToClient(new HPacket("ObjectUpdate", HMessage.Direction.TOCLIENT, selectedIdTable, uniqueId, 0, 0
                    , 1, "0.0", "0.0", 0, 1, 5, "state", "0", "offsetZ", String.valueOf(sliderOffSetZ.getValue()),
                    "offsetY", String.valueOf(sliderOffSetY.getValue()), "imageUrl", selectedUrlTable,
                    "offsetX", String.valueOf(sliderOffSetX.getValue()), -1, 1, 12345, false, false));
        }catch (Exception ignored){}
    }

    public void handleOpenLink() throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://www.youtube.com/JuliantyScripting"));
    }
}

//choiceBox.getItems().addAll('l', 'r'); // Agrega varios items
//choiceBox.getSelectionModel().select(0); // Inicializa con el valor en esa posicion
//choiceBox.setValue("r"); // Otra forma de hacerlo, no recomendable en bases de datos