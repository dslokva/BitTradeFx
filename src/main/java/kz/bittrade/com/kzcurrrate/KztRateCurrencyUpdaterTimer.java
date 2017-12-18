package kz.bittrade.com.kzcurrrate;

import kz.bittrade.com.BFConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimerTask;

public class KztRateCurrencyUpdaterTimer extends TimerTask {

    public KztRateCurrencyUpdaterTimer() {

    }

    @Override
    public void run() {
        try {
            System.out.println("[INFO] KZT currency rates updater started");
            KztRateCurrencyHolder kztRateCurrencyHolder = KztRateCurrencyHolder.getInstance();

            URL url = new URL(BFConstants.NATIONAL_BANK_CURR_RATE_UPDATE_URL);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(url.openStream());
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("item");
            if (nList == null || nList.getLength() <= 0) {
                System.out.println("[ERROR] KZT currency rates updater cant proceed (\"item\" nodes count = 0)");
                return;
            }

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String title = eElement.getElementsByTagName("title").item(0).getTextContent();
                    if (title.equalsIgnoreCase("USD") || title.equalsIgnoreCase("EUR")) {
                        String rateStr = eElement.getElementsByTagName("description").item(0).getTextContent();
                        String changeStr = eElement.getElementsByTagName("change").item(0).getTextContent();
                        if (changeStr.startsWith("+")) changeStr = changeStr.substring(1);

                        String dateStr = eElement.getElementsByTagName("pubDate").item(0).getTextContent();

                        DateFormat format = new SimpleDateFormat("MM.dd.yy", Locale.ENGLISH);
                        Date date = format.parse(dateStr);

                        KztRateCurrency kztRateCurrency = new KztRateCurrency();
                        kztRateCurrency.setCurrencyName(title);
                        kztRateCurrency.setRate(Double.parseDouble(rateStr));
                        kztRateCurrency.setChange(Double.parseDouble(changeStr));
                        kztRateCurrency.setUpdateDate(date);
                        kztRateCurrencyHolder.addKztCurrencyRate(kztRateCurrency);
                    }
                }
            }
            if (kztRateCurrencyHolder.getKztRatesMap().size() != 2) {
                System.out.println("[ERROR] KZT currency rates updater task ended successfully, but no USD & EUR pairs found in nationalbank.kz xml answer");
            } else {
                System.out.println("[INFO] KZT currency rates updater task ended successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
