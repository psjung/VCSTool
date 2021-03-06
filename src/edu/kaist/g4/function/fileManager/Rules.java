package edu.kaist.g4.function.fileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

import edu.kaist.g4.data.Architecture;
import edu.kaist.g4.data.ArchitectureElement;
import edu.kaist.g4.data.ArchitectureModel;
import edu.kaist.g4.data.ElementType;
import edu.kaist.g4.data.Relation;
import edu.kaist.g4.data.RelationType;
import edu.kaist.g4.data.TraceabilityLink;
import edu.kaist.g4.data.ViewType;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitecturalDifferentiations;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitectureChange;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitectureChangeDecision;
import edu.kaist.g4.data.architecturalDifferentiations.ChangeOperationTypes;

public class Rules{
    private String linkSrcId;
    private ArrayList<Object> linkDstId;
    private Vector<String> dstIdList;
    
    public void executeReadRule(Model_XML modelXML, Traceability_XML tLinkXML, String qName, Attributes attributes){
        if (qName.equals("XMI")){
            modelXML.setId(attributes.getValue("timestamp"));   //project id
        }
        else if(qName.equals("UML:Type")){
            String type = attributes.getValue("name");
            if(type.equals("Class Model")){
                modelXML.setType(ViewType.MODULE);
                modelXML.setId(attributes.getValue("xmi.id"));
            }
            else if(type.equals("Component Model")){
                modelXML.setType(ViewType.CNC);
                modelXML.setId(attributes.getValue("xmi.id"));
            }
/*            else if(type.equals("Traceability Model")){
                tLinkXML.setSrcModelID(attributes.getValue("srcModel"));
                tLinkXML.setDstModelID(attributes.getValue("dstModel"));
            }
*/
        }
        //Class Model(Module View)
        else if(qName.equals("UML:Class")) {
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(ElementType.MODULE);
            list.add(attributes.getValue("name"));
            list.add(attributes.getValue("VersionNo"));
            modelXML.getElements().put(attributes.getValue("xmi.id"), list);
        }
        else if(qName.equals("UML:Package")) {          //하위 class 읽을 필요
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(ElementType.PACKAGE);               
            list.add(attributes.getValue("name"));
            list.add(attributes.getValue("VersionNo"));
            modelXML.getElements().put(attributes.getValue("xmi.id"), list);
        }
        else if(qName.equals("UML:Generalization")){
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(RelationType.GENERALIZATION);
            list.add(attributes.getValue("subtype"));
            list.add(attributes.getValue("supertype"));
            modelXML.getRelations().add(list);
        }
        else if(qName.equals("UML:Dependency")){
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(RelationType.DEPENDENCY);
            list.add(attributes.getValue("client"));
            list.add(attributes.getValue("supplier"));
            modelXML.getRelations().add(list);
        }
        //Component Model(CNC View)
        else if(qName.equals("UML:Component")){
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(ElementType.COMPONENT);
            list.add(attributes.getValue("name"));
            list.add(attributes.getValue("VersionNo"));
            modelXML.getElements().put(attributes.getValue("xmi.id"), list);
        }
        else if(qName.equals("UML:Connector")){
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(ElementType.CONNECTOR);
            list.add(attributes.getValue("name"));
            list.add(attributes.getValue("VersionNo"));
            modelXML.getElements().put(attributes.getValue("xmi.id"), list);
        }
        else if(qName.equals("UML:Association")){
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(RelationType.RELATION);
            list.add(attributes.getValue("client"));
            list.add(attributes.getValue("supplier"));
            modelXML.getRelations().add(list);
        }
        //Traceability Links
        else if(qName.equals("UML:TraceLink")){
            linkSrcId = attributes.getValue("src");
            linkDstId = new ArrayList<Object>();
            dstIdList = new Vector<String>();
            linkDstId.add(attributes.getValue("srcModel"));
            linkDstId.add(attributes.getValue("dstModel"));
//            tLinkXML.getLinks().put(attributes.getValue("src"), );
        }
    }
    
    //하위 tag가 여러개인 경우
    public void endReadRule(Traceability_XML tLinkXML, String qName, String text){
        if(qName.equals("UML:TraceLink")){
            linkDstId.add(dstIdList);
            tLinkXML.getLinks().put(linkSrcId, linkDstId);
        }
        else if(qName.equals("dst")){
            dstIdList.add(text);
        }
    }
    
    public void executeWriteRule(Architecture arch, String dir){
      //XMLWriter variables
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();          

            /*architectureModel write*/
            Vector<ArchitectureModel> archModels = arch.getArchitectureModels();
            for(ArchitectureModel archModel : archModels){
                Document doc = docBuilder.newDocument();

                Element childElement = null;
                Element rootElement;
                Attr attr;
                
                //root element
                rootElement = doc.createElement("XMI");
                attr = doc.createAttribute("timestamp");
                attr.setValue(Long.toString(System.currentTimeMillis()));
                rootElement.setAttributeNode(attr);
                attr = doc.createAttribute("xmlns:UML");
                attr.setValue("omg.org/UML1.3");
                rootElement.setAttributeNode(attr);
                doc.appendChild(rootElement);
                
                //UML:Package tag(module view ? or CNC view ?)
                Element typeElement = doc.createElement("UML:Type");
                attr = doc.createAttribute("name");
                
                ViewType vType = archModel.getType();
                if(vType == ViewType.MODULE){
                    attr.setValue("Class Model");
                }
                else if(vType == ViewType.CNC){
                    attr.setValue("Component Model");
                }
                typeElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("xmi.id");
                attr.setValue(archModel.getId());
                typeElement.setAttributeNode(attr);
                rootElement.appendChild(typeElement);
                
                //add element
                Iterator<ArchitectureElement> archElementIt;
                Iterator<Relation> relationIt;
                archElementIt = archModel.getElements().values().iterator();
                while(archElementIt.hasNext()){
                    ArchitectureElement archElement = (ArchitectureElement)archElementIt.next();
                    
                    //write archElement
                    ElementType eType = archElement.getType();
                    if(eType == ElementType.MODULE){
                        childElement = doc.createElement("UML:Class");
                    }
                    if(eType == ElementType.PACKAGE){
                        childElement = doc.createElement("UML:Package");
                    }
                    else if(eType == ElementType.COMPONENT){
                        childElement = doc.createElement("UML:Component");
                    }
                    else if(eType == ElementType.CONNECTOR){
                        childElement = doc.createElement("UML:Connector");
                    }   
                    
                    attr = doc.createAttribute("name");     //name
                    attr.setValue(archElement.getName());
                    childElement.setAttributeNode(attr);
                    
                    attr = doc.createAttribute("xmi.id");   //xmi.id
                    attr.setValue(archElement.getId());
                    childElement.setAttributeNode(attr);
                    
                    attr = doc.createAttribute("revisionNo");   //# of revision
                    attr.setValue(Integer.toString(archElement.getRevision()));
                    childElement.setAttributeNode(attr);
                    
                    typeElement.appendChild(childElement);
                }
                
                //add relations
                relationIt = archModel.getRelations().iterator();
                while(relationIt.hasNext()){
                    Relation rel = (Relation)relationIt.next();
                    
                    //write rel
                    RelationType rType = rel.getType();
                    if(rType == RelationType.DEPENDENCY){
                        childElement = doc.createElement("UML:Dependency");
                        
                        attr = doc.createAttribute("client");   //client
                        attr.setValue(rel.getSource().getId());
                        childElement.setAttributeNode(attr);
                        
                        attr = doc.createAttribute("supplier");   //supplier
                        attr.setValue(rel.getDestination().getId());
                        childElement.setAttributeNode(attr);
                    }
                    else if(rType == RelationType.RELATION){
                        childElement = doc.createElement("UML:Association");
                        
                        attr = doc.createAttribute("client");   //client
                        attr.setValue(rel.getSource().getId());
                        childElement.setAttributeNode(attr);
                        
                        attr = doc.createAttribute("supplier");   //supplier
                        attr.setValue(rel.getDestination().getId());
                        childElement.setAttributeNode(attr);
                    }
                    else if(rType == RelationType.GENERALIZATION){
                        childElement = doc.createElement("UML:Generalization");
                        
                        attr = doc.createAttribute("subtype");   //subtype
                        attr.setValue(rel.getSource().getId());
                        childElement.setAttributeNode(attr);
                        
                        attr = doc.createAttribute("supertype");   //supertype
                        attr.setValue(rel.getDestination().getId());
                        childElement.setAttributeNode(attr);
                    }
                    typeElement.appendChild(childElement);
                }
                
                // XML write 
                TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
                Transformer transformer = transformerFactory.newTransformer(); 
         
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); 
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");        
                
                DOMSource source = new DOMSource(doc); 
                StreamResult result = new StreamResult(new FileOutputStream(new File(dir + "/Model/"+vType.toString()+"_"+archModel.getId()+".xml"))); 
         
                transformer.transform(source, result);
            }
            
            /*Traceability write*/
            Document doc = docBuilder.newDocument();
            Element traceLinkElement = null;
            Element rootElement;
            Attr attr;
            
            //root element
            rootElement = doc.createElement("XMI");
            attr = doc.createAttribute("timestamp");
            attr.setValue(Long.toString(System.currentTimeMillis()));
            rootElement.setAttributeNode(attr);
            attr = doc.createAttribute("xmlns:UML");
            attr.setValue("omg.org/UML1.3");
            rootElement.setAttributeNode(attr);
            doc.appendChild(rootElement);
            
            Element typeElement = doc.createElement("UML:Type");
            attr = doc.createAttribute("name");
            attr.setValue("Traceability Model");
            typeElement.setAttributeNode(attr);
            rootElement.appendChild(typeElement);
            
            Vector<TraceabilityLink> tLinks = arch.gettLinks();
            for(TraceabilityLink link : tLinks){
                traceLinkElement = doc.createElement("UML:TraceLink");
                
                attr = doc.createAttribute("src");
                attr.setValue(link.getSource().getId());
                traceLinkElement.setAttributeNode(attr);
                typeElement.appendChild(traceLinkElement);
                
                attr = doc.createAttribute("srcModel");
                attr.setValue(link.getSourceModel().getId());
                traceLinkElement.setAttributeNode(attr);
                typeElement.appendChild(traceLinkElement);
                
                attr = doc.createAttribute("dstModel");
                attr.setValue(link.getDestModel().getId());
                traceLinkElement.setAttributeNode(attr);
                typeElement.appendChild(traceLinkElement);
                
                //add element
                Vector<ArchitectureElement> dstElements = (Vector<ArchitectureElement>)link.getDestination();
                for(ArchitectureElement ae : dstElements){
                    Element dst = doc.createElement("dst");
                    dst.appendChild(doc.createTextNode(ae.getId()));
                    traceLinkElement.appendChild(dst);
                }
            }
         // XML write 
            TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
            Transformer transformer = transformerFactory.newTransformer(); 
     
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");        
            
            DOMSource source = new DOMSource(doc); 
            StreamResult result = new StreamResult(new FileOutputStream(new File(dir + "/Traceability/traceability.xml"))); 
     
            transformer.transform(source, result);
            
        }catch(ParserConfigurationException pce){
            pce.printStackTrace();
        }catch(TransformerException tfe){
            tfe.printStackTrace();
        }catch(FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }
    }
    
    public void appendDiffList(ArchitecturalDifferentiations diffList, String dir){
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement, segmentElement;
            Attr attr;
            
            File diffFile = new File(dir + "/Differences.xml");
            
            if(diffFile.exists() == false){
                //root element
                rootElement = doc.createElement("Revision");
                attr = doc.createAttribute("timestamp");
                attr.setValue(Long.toString(System.currentTimeMillis()));
                rootElement.setAttributeNode(attr);
                attr = doc.createAttribute("xmlns:Revision");
                attr.setValue("blank");
                rootElement.setAttributeNode(attr);
                doc.appendChild(rootElement);
            }
            else{
                doc = docBuilder.parse(diffFile);
                rootElement = doc.getDocumentElement();
            }
            segmentElement = doc.createElement("ChangeDecision");
            rootElement.appendChild(segmentElement);
            attr = doc.createAttribute("id");
            attr.setValue(Long.toString(System.currentTimeMillis()));
            segmentElement.setAttributeNode(attr);
            attr = doc.createAttribute("message");
            attr.setValue(diffList.getArchitectureChangeDecision().getArchitectureChangeDrivers());
            segmentElement.setAttributeNode(attr);
            
            Vector<ArchitectureChange> changes = diffList.getArchitectureChanges();
            for(ArchitectureChange change : changes){
                Element diffElement = doc.createElement("ArchitectureChange");
                attr = doc.createAttribute("id");
                attr.setValue(change.getParameter());
                diffElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("operation");
                attr.setValue(change.getChangeOperation().toString());
                diffElement.setAttributeNode(attr);
                
                attr = doc.createAttribute("message");
                attr.setValue(change.getMessage());
                diffElement.setAttributeNode(attr);
                
                segmentElement.appendChild(diffElement);
            }
            
            // XML write 
            TransformerFactory transformerFactory = TransformerFactory.newInstance(); 
            Transformer transformer = transformerFactory.newTransformer(); 
     
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");        
            
            DOMSource source = new DOMSource(doc); 
            StreamResult result = new StreamResult(new FileOutputStream(new File(dir + "/Differences.xml"))); 
     
            transformer.transform(source, result);

            
        }catch(ParserConfigurationException pce){
            pce.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
    }
    
    public Vector<ArchitecturalDifferentiations> readDiffList(String dir){
        //TODO
        
        Vector<ArchitecturalDifferentiations> diffLists = null;
        
        try{
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance(); 
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement;
            
            File diffFile = new File(dir + "/Differences.xml");
            
            if(diffFile.exists() == false){
                //root element
                System.out.println("diff file not exist");
                return null;
            }
            else{
                doc = docBuilder.parse(diffFile);
                rootElement = doc.getDocumentElement();
            }
            
            diffLists = new Vector<ArchitecturalDifferentiations>();
            
            rootElement.normalize();
            NodeList nodeList = doc.getElementsByTagName("ChangeDecision");

            // each Revision
            for(int i=0; i<nodeList.getLength(); i++){          
                Node node  = nodeList.item(i);
                Element decision = (Element)node;
                // ChangeDecision
//                Element decision = (Element)(revision.getElementsByTagName("ChangeDecision").item(0));
                //All ArchitectureChange List 
                NodeList changes = decision.getElementsByTagName("ArchitectureChange");

                //make vector for ArchitectureChange
                Vector<ArchitectureChange> architectureChange = new Vector<ArchitectureChange>();
                for(int j=0; j<changes.getLength();j++){
                    
                    //make object of each ArchitecureChange and push them into vector
                    Element change = (Element)changes.item(j);
                    //constructor parametor
                    String operationType = change.getAttribute("operation");
                    String id = change.getAttribute("id");
                    String message = change.getAttribute("message");
                    
                    ArchitectureChange c;
                    if(operationType.equals("ADD")){
                        c = new ArchitectureChange(ChangeOperationTypes.ADD, id, message);                        
                    }
                    else if(operationType.equals("DELETE")){
                        c = new ArchitectureChange(ChangeOperationTypes.DELETE, id, message);
                    }
                    else{
                        c = new ArchitectureChange(ChangeOperationTypes.MODIFY, id, message);
                    }
                    
                    architectureChange.add(c);
                }
                String changeDecision = decision.getAttribute("message");
                String id = decision.getAttribute("id");
                
                //from revision attr
//                String timestamp = revision.getAttribute("timestamp");
                
                ArchitectureChangeDecision architectureChangeDecision = new ArchitectureChangeDecision();
                architectureChangeDecision.setArchitectureChangeDrivers(changeDecision);
                architectureChangeDecision.setArchitectureChanges(architectureChange);
                architectureChangeDecision.setId(id);
                
                ArchitecturalDifferentiations ad = new ArchitecturalDifferentiations(architectureChangeDecision, architectureChange);
                ad.setId(id);
 //               ad.setTimestamp(timestamp);
                diffLists.add(ad);
            }
            

            
        }catch(ParserConfigurationException pce){
            pce.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
            
        return diffLists;
        
    }
}
 
