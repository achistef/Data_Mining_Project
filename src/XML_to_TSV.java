import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.PrintWriter;

public class XML_to_TSV {

    private final SAXParserFactory factory;
    private final SAXParser saxParser;
    private final DefaultHandler handler;
    private final PrintWriter type1;
    private final PrintWriter type2;
    private final StringBuilder sb;

    public XML_to_TSV(String out1, String out2) throws Exception{
        this.factory = SAXParserFactory.newInstance();
        this.factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        this.saxParser = factory.newSAXParser();
        type1 = new PrintWriter(out1);
        type1.print("ID\t Accepted_Answer_ID\tCreation_Date\tScore\tView_Count\tOwner_ID\tTags\n");
        type2 = new PrintWriter(out2);
        type2.print("ID\t Parent_ID\tCreation_Date\tScore\tOwner_ID\n");
        sb = new StringBuilder();

        this.handler = new DefaultHandler(){

            public void startElement(String uri, String localName,String qName, Attributes attributes){
                if (qName.equals("row")) {
                    String type = attributes.getValue("PostTypeId");
                    if(type.equals("1")){
                        saveType1(attributes);
                    }
                    if(type.equals("2")) {
                        saveType2(attributes);
                    }
                }

            }
        };
    }

    public void convert(String input) throws Exception{
        saxParser.parse(input, handler);
        type1.close();
        type2.close();
    }

    private void saveType1(Attributes attr){
        sb.append(attr.getValue("Id"));
        sb.append("\t");
        sb.append(attr.getValue("AcceptedAnswerId"));
        sb.append("\t");
        sb.append(attr.getValue("CreationDate"),0, 10);
        sb.append("\t");
        sb.append(attr.getValue("Score"));
        sb.append("\t");
        sb.append((attr.getValue("ViewCount")));
        sb.append("\t");
        sb.append(attr.getValue("OwnerUserId"));
        sb.append("\t");
        String tags = attr.getValue("Tags").replaceAll("<", "").replaceAll(">", ",");
        sb.append(tags,0, tags.length()-1);
        sb.append("\n");
        type1.print(sb.toString());
        sb.setLength(0); //clear
    }

    private void saveType2(Attributes attr){
        sb.append(attr.getValue("Id"));
        sb.append("\t");
        sb.append(attr.getValue("ParentId"));
        sb.append("\t");
        sb.append(attr.getValue("CreationDate"), 0, 10);
        sb.append("\t");
        sb.append(attr.getValue("Score"));
        sb.append("\t");
        sb.append(attr.getValue("OwnerUserId"));
        sb.append("\n");
        type2.print(sb.toString());
        sb.setLength(0); //clear

    }

    public static void main(String[] args) throws Exception{
        String inputFile = args[0];
        String outputFile1 = args[1]; // posts 1
        String outputFile2 = args[2]; // posts 2
        XML_to_TSV conv = new XML_to_TSV(outputFile1, outputFile2);
        conv.convert(inputFile);
    }
}
