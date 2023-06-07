package org.example;

import javax.swing.*;

import Manager.GSM1;
import Manager.ModelView;


public class Main {
    public static void main(String[] args) {

        String modelPath = "src/test/resources/data/test/siena.xml";
        String infoModelPath = "src/test/resources/data/test/infoModel.xsd";
        GSM1 gsm = new GSM1(modelPath, infoModelPath);
        gsm.init();

        gsm.setInfoElement("Container", "EmptyShippingHooked");
        gsm.setInfoElement("Truck", "ProducerStill");
        gsm.triggerEvent("Truck_e");

        gsm.triggerEvent("BoundaryEvent_2");


        System.out.println(GSM1.documentToString(gsm.getInfoModel()));

        JFrame frame = new JFrame("XML Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//        InfoModelView imv = new InfoModelView(gsm);
//        frame.getContentPane().add(imv);

        ModelView modelView = new ModelView(gsm);
        frame.getContentPane().add(modelView);

        frame.pack();
        frame.setVisible(true);

    }
}



