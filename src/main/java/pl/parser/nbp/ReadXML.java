package pl.parser.nbp;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class ReadXML {

    private String currency;
    private String startDate;
    private String endDate;
    private List<String> price;
    private List<String> sellPrice;

    String startUrl = "http://www.nbp.pl/kursy/xml/dir.txt";
    String endUrl = "http://www.nbp.pl/kursy/xml/dir.txt";

    public ReadXML(String currency, String startDate, String endDate) {
        this.currency = currency;
        this.startDate = startDate;
        this.endDate = endDate;
        price = new ArrayList<>();
        sellPrice = new ArrayList<>();
    }

    public void read() {

        prepareStartURL();
        prepareEndURL();
        LinkedList<String> list = new LinkedList<String>();

        if (checkIfYeatIsEqual(getStartDate(),getEndDate())){


            try {
                final Document document = Jsoup.connect(startUrl).get();
                Elements press = document.select("body");
                String[] text = press.text().split(" ");

                String searchFor = getStartDate();
                searchFor = searchFor.substring(2,4) + searchFor.substring(5,7) + searchFor.substring(8,10);
                String endOfSearch = getEndDate();
                endOfSearch = endOfSearch.substring(2,4) + endOfSearch.substring(5,7) + endOfSearch.substring(8,10);

                String tmp = text[0];
                boolean flag = false;
                for (int i = 0; i < text.length && !flag; i++) {
                    if (text[i].substring(5).equals(searchFor)) {
                        for (int j = i; j < text.length; j++ ) {
                            if (tmp.substring(8).equals(text[j].substring(8)))
                                continue;
                            tmp = text[j];
                            list.add(text[j]);

                            if (text[j].substring(5).equals(endOfSearch)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                }

                Document document2;
                for (String el: list) {
                    String conn = startUrl.substring(0,28) + el + ".xml";

                    try {
                        document2 = Jsoup.connect(conn).parser(Parser.xmlParser()).get();
                        Elements press2 = document2.select("pozycja");
                        String[] t = press2.text().split(" ");
                        for (int i = 0; i<t.length; i++) {
                            if (t[i].equals(getCurrency())) {
                                if (t[i+1].matches("^[0-9]+.*$") && t[i+2].matches("^[0-9]+.*$")) {
                                    price.add(t[i + 1]);
                                    sellPrice.add(t[i + 2]);
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        System.out.println(e.getStackTrace());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {   //Not the same year

            List<String> yearList = countDiff();
            try {
                boolean flag2 = false;
                for (int x=0; x<yearList.size(); x++) {
                    Document document = Jsoup.connect(yearList.get(x)).get();
                    Elements press = document.select("body");
                    String[] text = press.text().split(" ");

                    String searchFor = getStartDate();
                    searchFor = searchFor.substring(2, 4) + searchFor.substring(5, 7) + searchFor.substring(8, 10);
                    String endOfSearch = getEndDate();
                    endOfSearch = endOfSearch.substring(2, 4) + endOfSearch.substring(5, 7) + endOfSearch.substring(8, 10);

                    String tmp = text[0];
                    boolean flag = false;

                    for (int i = 0; i < text.length && !flag; i++) {
                        if (text[i].substring(5).equals(searchFor) || flag2) {
                            for (int j = i; j < text.length; j++) {
                                if (tmp.substring(8).equals(text[j].substring(8)))
                                    continue;
                                tmp = text[j];
                                list.add(text[j]);

                                if (text[j].substring(5).equals(endOfSearch)) {
                                    flag = true;
                                    break;
                                }
                            }
                        }
                    }

                    Document document2;
                    for (String el: list) {
                        String conn = startUrl.substring(0,28) + el + ".xml";

                        try {
                            document2 = Jsoup.connect(conn).parser(Parser.xmlParser()).get();
                            Elements press2 = document2.select("pozycja");
                            String[] t = press2.text().split(" ");
                            for (int i = 0; i<t.length; i++) {
                                if (t[i].equals(getCurrency())) {
                                    if (t[i+1].matches("^[0-9]+.*$") && t[i+2].matches("^[0-9]+.*$")) {
                                        price.add(t[i + 1]);
                                        sellPrice.add(t[i + 2]);
                                    }
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.out.println(e.getStackTrace()+"n1");
                        }
                    }
                    flag2 = true;
                }
            } catch(Exception e) {
                System.out.println(e.getStackTrace()+"n2");
            }
        }
    }

    public List<String> countDiff() {
        List<String> yearList = new ArrayList<String>();
        int count = Integer.parseInt(getEndDate().substring(2,4)) - Integer.parseInt(getStartDate().substring(2,4));
        int start = Integer.parseInt(getStartDate().substring(2,4));
        for (int i=0; i<count+1; i++) {
            yearList.add(startUrl.substring(0,31) + "20" + (start++) + ".txt" );
        }

        return yearList;
    }

    public void writeAverage() {
        List<String> l = getPrice();
        Float avg = 0.0f;
        for (int i=0; i<l.size(); i++) {
            avg += Float.valueOf(l.get(i).replace(",","."));
        }
        System.out.println(avg/l.size());
    }

    public boolean checkIfDateIsCorrect(String data) {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (data.substring(0,4).equals(String.valueOf(year)))
            return true;
        return false;
    }

    public void prepareStartURL() {
        if (checkIfDateIsCorrect(startDate)) {
            return;
        } else {
            String s = getStartDate().substring(0,4);
            startUrl = startUrl.substring(0,31) + s + ".txt";
        }
    }

    public void prepareEndURL() {
        if (checkIfDateIsCorrect(startDate)) {
            return;
        } else {
            String s = getEndDate().substring(0,4);
            endUrl = endUrl.substring(0,31) + s + ".txt";
        }
    }

    public boolean checkIfYeatIsEqual(String startDate, String endDate) {
        if (startDate.substring(0,4).equals(endDate.substring(0,4))){
            return true;
        }
        return false;
    }

    public void writeCalculation() {
        List<String> l = getSellPrice();
        Float avg = 0.0f;
        for (int i=0; i<l.size(); i++) {
            try {
                avg += Float.valueOf(l.get(i).replace(",", "."));
            } catch (Exception e) {
                System.out.println(i);
            }
        }
        avg /= l.size();
        float sum = 0.0f;
        for (int j=0; j<l.size(); j++) {
            sum += Math.pow((Float.valueOf(l.get(j).replace(",",".")) - avg),2);
        }
        sum = (float) Math.sqrt(sum/l.size());
        System.out.println(sum);
    }

    public List<String> getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(List<String> sellPrice) {
        this.sellPrice = sellPrice;
    }

    public List<String> getPrice() {
        return price;
    }

    public void setPrice(List<String> price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
