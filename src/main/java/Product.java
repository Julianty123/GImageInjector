import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// This constructor class is very important to be able of create the tableView
public class Product {
    private SimpleStringProperty productImageUrl;
    private SimpleIntegerProperty productFurniID;
    private SimpleDoubleProperty productOffSetX;
    private SimpleDoubleProperty productOffSetY;
    private SimpleDoubleProperty productOffSetZ;

    public Product(String productImageUrl, int productFurniID, double productOffSetX, double productOffSetY, double productOffSetZ){
        this.productImageUrl = new SimpleStringProperty(productImageUrl);
        this.productFurniID = new SimpleIntegerProperty(productFurniID);
        this.productOffSetX = new SimpleDoubleProperty(productOffSetX);
        this.productOffSetY = new SimpleDoubleProperty(productOffSetY);
        this.productOffSetZ = new SimpleDoubleProperty(productOffSetZ);
    }

    public  String getProductImageUrl(){
        return productImageUrl.get();
    }

    public  int getProductFurniID(){
        return productFurniID.get();
    }

    public double getProductOffSetX(){ return productOffSetX.get(); }

    public double getProductOffSetY(){ return productOffSetY.get(); }

    public double getProductOffSetZ(){ return productOffSetZ.get(); }
}
