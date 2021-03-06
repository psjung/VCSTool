package edu.kaist.g4.data;

import java.util.Vector;

/**
 * 
 * @author Junhaeng Heo
 *
 */

public interface IArchitecture {
    
    //init architecture object
    public void addArchitectureModel(ViewType type, ArchitectureModel model);
    public void addArchitectureElement(ViewType type,String name, ArchitectureElement ae);
    public void addRelation(ViewType type,String name, Relation r);
    
    public ArchitectureModel getView(ViewType type,String name);
 
    
    public boolean addTracebilityLink(String sourceId, Vector<String> destId); //using this method
    public boolean addTracebilityLink(String sourceId, Vector<String> destId, ArchitectureModel sorcemodel, ArchitectureModel destmodel); //not use

 
    
    
    //get information of architecture
    public String overallInformation();
    
    /**
     * @Method Name : getArchitectureModels
     * @Detail      : 해당 Architecture에 있는 Architecture Model들의 리스트를 리턴하는 함수.
     *
     * @return
     */
    public Vector<ArchitectureModel> getArchitectureModels();
    public ArchitectureModel getArchitectureModelById(String ID);
    
    
}
