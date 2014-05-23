package edu.kaist.g4.function.fileManager;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import edu.kaist.g4.data.Architecture;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitecturalDifferentiations;

public class FileManager implements IFileManager{
    
    // TODO: Singleton 으로 할 필요가 있을까? Github 이슈 게시판 참조.
    private Reader reader;
    private Writer writer;
    
    public FileManager(){
        reader = new Reader();
        writer = new Writer();
    }

    @Override
    public Architecture readWorkingArchitecture(String filePath) {  //dir path??
        try{
            File file = new File(filePath);
            File[] listFiles = file.listFiles();
            for(File f : listFiles){   
                SAXParserFactory spf = SAXParserFactory.newInstance();  
                SAXParser sp = spf.newSAXParser();  
                XMLReader xr = sp.getXMLReader();  
                    
                xr.setContentHandler(reader);  
        
                xr.parse(new InputSource(new FileInputStream(f)));
                reader.addArchitectureModel();
            }
        } catch (Exception e) {  
            e.printStackTrace();  
        }
        return reader.getArchitecture();
    }

    @Override
    public Architecture readRecentArchitecture() {
        try{
            SAXParserFactory spf = SAXParserFactory.newInstance();  
            SAXParser sp = spf.newSAXParser();  
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(reader);
            
            File mFile = new File("version1.0/Model");
            File[] mListFiles = mFile.listFiles();
            
            for(File f : mListFiles){     
                xr.parse(new InputSource(new FileInputStream(f)));
                reader.addArchitectureModel();
            }
            
            File tFile = new File("version1.0/Traceability");
            File[] tListFiles = tFile.listFiles();
            
            for(File f : tListFiles){     
                xr.parse(new InputSource(new FileInputStream(f)));
                reader.addTraceability();
            }
        } catch (Exception e) {  
            e.printStackTrace();  
        }
        return reader.getArchitecture();
    }

    @Override
    public void writeNewRecentArchitecture(Architecture workingArchitecture) {
        writer.writeArchitecture(workingArchitecture, "version1.1");
    }

    @Override
    public void appendDiffList(ArchitecturalDifferentiations addedArchitecturalDifferentiations) {
        
    }

}
