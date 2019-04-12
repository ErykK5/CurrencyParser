package pl.parser.nbp;


import java.util.List;

public class MainClass {

    public static void main(String[] args) {

        if ( args.length != 3 ){
            System.out.println("Usage: <Currency> <From> <To>");
            return;
        }

        ReadXML readXML = new ReadXML(args[0],args[1],args[2]);
        readXML.read();
        readXML.writeAverage();
        readXML.writeCalculation();
    }
}
