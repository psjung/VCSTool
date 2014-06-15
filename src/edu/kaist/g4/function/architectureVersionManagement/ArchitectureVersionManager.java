package edu.kaist.g4.function.architectureVersionManagement;

import java.util.Vector;

import edu.kaist.g4.data.Architecture;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitecturalDifferentiations;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitectureChange;
import edu.kaist.g4.data.architecturalDifferentiations.ArchitectureChangeDecision;
import edu.kaist.g4.function.fileManager.FileManager;
import edu.kaist.g4.function.fileManager.IFileManager;

/**
 * 
 * @FileName : ArchitectureVersionManager.java
 * @Package  : edu.kaist.g4.function.architectureVersionManagement
 * @Author   : Hwi Ahn (ahnhwi@kaist.ac.kr)
 * @Date     : 2014
 * @Detail   :
 * 
 */
/**
 * 
 * @author : Junhaeng Heo
 * Implement IArchitectureVersionManager
 *
 */
public class ArchitectureVersionManager implements IArchitectureVersionManager{
    
    //TODO: 차후 수정.
    //TODO: Constructor에서 초기화하는 걸로 수정.
    IFileManager fileManager = new FileManager();
    
    //checkout에서도 recentArchitecture를 사용하기 때문에 멤버변수로 선언
    Architecture recentArchitecture;
    
    //diff 요청할 때마다 filemanager를 불러오는걸 막기 위해 멤버변수로 선언
    ArchitecturalDifferentiations currentDiffList;
   
    
    @Override
    public String commitNewArchitecture(String dirPathforNewArchitecture, String changeDecision) {
        //TODO: 여기다 Sequence 다이어그램 내용 넣기
        Architecture workingArchitecture = fileManager.readWorkingArchitecture(dirPathforNewArchitecture);
        
        if(recentArchitecture == null)
            recentArchitecture = fileManager.readRecentArchitecture();
        
        INewVersionGenerator newVersionGenerator = new NewVersionGenerator(workingArchitecture, recentArchitecture);
        Vector<ArchitectureChange> architectureChange = newVersionGenerator.buildNewVersion();
        if(architectureChange.size() == 0){
            return null;
        }
        //TODO: Change Decision에 관련된 내용을 여기에 넣기
        ArchitectureChangeDecision architectureChangeDecision = new ArchitectureChangeDecision();
        architectureChangeDecision.setArchitectureChangeDrivers(changeDecision);
        architectureChangeDecision.setArchitectureChanges(architectureChange);
        
//        workingArchitecture = fileManager.readWorkingArchitecture(dirPathforNewArchitecture);   //깊은 복사가 없어서 일단 다시 불러옴
        ArchitecturalDifferentiations newDiffList = new ArchitecturalDifferentiations(architectureChangeDecision, architectureChange);
        
        fileManager.removeRecentArchitecture();
        fileManager.writeNewRecentArchitecture(workingArchitecture);
        fileManager.appendDiffList(newDiffList);
        
        return newDiffList.getArchitectureChanges().size() + " architecture changes derived from " + newDiffList.getArchitectureChangeDecision().getArchitectureChangeDrivers();
    }

    @Override
    public String showRecentArchitecture() {
        // TODO Auto-generated method stub
        if(recentArchitecture == null)
            recentArchitecture = fileManager.readRecentArchitecture();
        
        return recentArchitecture.overallInformation();
        
    }

    @Override
    public String traceVersionInfoWith(String command, String parameter) {
        if(currentDiffList == null)
            currentDiffList = fileManager.readDiffList();
        IVersionInfoTracer versionInfoTracer = new VersionInfoTracer(currentDiffList);
        String printedMessage = "";
        if(command.equals("ViewAll")) //TODO: 향후 Enum으로 변경.
            printedMessage = versionInfoTracer.printAllDiffs(parameter);
        else
            printedMessage = "This is not a supported command."; //TODO: 향후 메세지들 한 곳에 모으기.
        return printedMessage;
    }
    
    public String getRecentArchPath() {
        // TODO Auto-generated method stub
        return "RecentArchitecture";
    }
    
    
}
