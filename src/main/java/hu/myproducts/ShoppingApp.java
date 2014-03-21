package hu.myproducts;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.List;

public class ShoppingApp {
    public int row = 0;
    public WritableWorkbook workbook;
    public WritableSheet sheet;

    public ShoppingApp() {
        try {
            workbook = Workbook.createWorkbook(new File("shoppingdotcom.xls"));
            sheet = workbook.createSheet("shopping.com", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getResponse(String url) {
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String body = null;
        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpResponse response = defaultHttpClient.execute(httpGet);
            body = responseHandler.handleResponse(response);
            defaultHttpClient.getConnectionManager().shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

    public Document readFromString(String xmlContent) {
        StringReader stringReader = new StringReader(xmlContent);
        try {
            return new SAXBuilder().build(stringReader);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void iterate(Element node, int depth, int col) throws WriteException {
        List<Element> children = node.getChildren();
        if (!(children.size() > 0) && node.getName().equals("category")) {
            this.sheet.addCell(new Label(col, this.row, node.getValue()));
            System.out.println(tabulate(depth)+ "Category name: " + node.getValue());
            depth -= 1;
            this.row += 1;
        }
        else {
            if (node.getName().equals("category"))
                col += 1;
            for(Element child : children) {
                if (child.getName().equals("category")) {
                    this.sheet.addCell(new Label(col, this.row, child.getChildren().get(0).getValue()));
                    System.out.println(tabulate(depth)+ "Category name: " + child.getChildren().get(0).getValue());
                    this.row += 1;
                }
                iterate(child, depth + 1, col);
            }
        }
    }

    public String tabulate(int depth) {
        String tab = new String();
        for (int i = 0; i < depth; i++) {
            tab += "----";
        }
        return tab;
    }

    public static void main(String[] args) throws WriteException, IOException {
        ShoppingApp shoppingApp = new ShoppingApp();
        String url = "http://sandbox.api.ebaycommercenetwork.com/publisher/3.0/rest/CategoryTree?apiKey=78b0db8a-0ee1-4939-a2f9-d3cd95ec0fcc&visitorUserAgent&visitorIPAddress&trackingId=7000610&categoryId=0&showAllDescendants=true";
        Document root = shoppingApp.readFromString(shoppingApp.getResponse(url));

        shoppingApp.iterate(root.getRootElement(), 0, 0);
        shoppingApp.workbook.write();
        shoppingApp.workbook.close();
    }
}
