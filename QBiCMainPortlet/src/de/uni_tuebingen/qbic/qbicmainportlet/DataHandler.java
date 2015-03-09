package de.uni_tuebingen.qbic.qbicmainportlet;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import model.DatasetBean;
import model.ExperimentBean;
import model.ExperimentType;
import model.ProjectBean;
import model.SampleBean;
import model.SpaceBean;
import parser.Parser;
import parser.PersonParser;
import persons.Qperson;
import properties.Qproperties;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.ControlledVocabularyPropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Image;
import com.vaadin.ui.ProgressBar;

import de.uni_tuebingen.qbic.util.DashboardUtil;
//
// class SpaceInformation {
// public int numberOfProjects;
// public int numberOfExperiments;
// public int numberOfSamples;
// public int numberOfDatasets;
// public String lastChangedExperiment;
// public String lastChangedSample;
// public Date lastChangedDataset;
// public IndexedContainer projects;
// public Set<String> members;
//
// public String toString() {
// return String.format(
// "#Projects: %s, #Exp, %s, #Samples %s, #Datasets %s, #containeritems %d, members: %s",
// numberOfProjects, numberOfExperiments, numberOfSamples, numberOfDatasets, projects.size(),
// members.toString());
//
// }
// }

//
// class ProjectInformation {
// public IndexedContainer experiments;
// public int numberOfExperiments;
// public int numberOfSamples;
// public int numberOfDatasets;
// public String lastChangedExperiment;
// public String lastChangedSample;
// public Date lastChangedDataset;
// public String description;
// public String statusMessage;
// public ProgressBar progressBar;
// public String contact;
// public Set<String> members;
// }
//
//
// class ExperimentInformation {
//
// public String experimentType;
// public int numberOfSamples;
// public int numberOfDatasets;
// public String lastChangedSample;
// public Date lastChangedDataset;
// public IndexedContainer samples;
// public Map<String, String> properties;
// public String propertiesFormattedString;
// public Map<String, List<String>> controlledVocabularies;
// public String identifier;
// }
//
//
// class SampleInformation {
//
// public String sampleType;
// public int numberOfDatasets;
// public Date lastChangedDataset;
// public HierarchicalContainer datasets;
// public Map<String, String> properties;
// public String propertiesFormattedString;
// // Map containing parents of the sample and the corresponding sample types
// public Map<String, String> parents;
// public String parentsFormattedString;
// public String xmlPropertiesFormattedString;
// }


public class DataHandler {


  // Map<String, SpaceInformation> spaces = new HashMap<String, SpaceInformation>();
  // Map<String, ProjectInformation> projectInformations = new HashMap<String,
  // ProjectInformation>();
  // Map<String, ExperimentInformation> experimentInformations =
  // new HashMap<String, ExperimentInformation>();
  // Map<String, SampleInformation> sampleInformations = new HashMap<String, SampleInformation>();
  Map<String, ProjectBean> projectMap =
      new HashMap<String, ProjectBean>();
  Map<String, ExperimentBean> experimentMap =
      new HashMap<String, ExperimentBean>();
  Map<String, SampleBean> sampleMap =
      new HashMap<String, SampleBean>();
  Map<String, DatasetBean> datasetMap =
      new HashMap<String, DatasetBean>();

  // Map<String, IndexedContainer> space_to_projects = new HashMap<String, IndexedContainer>();
  //
  // Map<String, IndexedContainer> space_to_experiments = new HashMap<String, IndexedContainer>();
  // Map<String, IndexedContainer> project_to_experiments = new HashMap<String, IndexedContainer>();
  //
  // Map<String, IndexedContainer> space_to_samples = new HashMap<String, IndexedContainer>();
  // Map<String, IndexedContainer> project_to_samples = new HashMap<String, IndexedContainer>();
  // Map<String, IndexedContainer> experiment_to_samples = new HashMap<String, IndexedContainer>();

  // Map<String, HierarchicalContainer> space_to_datasets =
  // new HashMap<String, HierarchicalContainer>();
  // Map<String, HierarchicalContainer> project_to_datasets =
  // new HashMap<String, HierarchicalContainer>();
  // Map<String, HierarchicalContainer> experiment_to_datasets =
  // new HashMap<String, HierarchicalContainer>();
  // Map<String, HierarchicalContainer> sample_to_datasets =
  // new HashMap<String, HierarchicalContainer>();

  List<SpaceWithProjectsAndRoleAssignments> space_list = null;
  // Map<String, IndexedContainer> connectedPersons = new HashMap<String, IndexedContainer>();
  IndexedContainer connectedPersons = new IndexedContainer();

  public List<SpaceWithProjectsAndRoleAssignments> getSpacesWithProjectInformation() {
    if (space_list == null) {
      space_list = this.openBisClient.getFacade().getSpacesWithProjects();
    }
    return space_list;
  }

  OpenBisClient openBisClient;


  public DataHandler(OpenBisClient client) {
    // reset(); //TODO useless?
    this.openBisClient = client;
  }


  // // id in this case meaning the openBIS instance ?!
  // public SpaceInformation getSpace(String identifier) throws Exception {
  //
  // List<SpaceWithProjectsAndRoleAssignments> space_list = null;
  // SpaceInformation spaces = null;
  //
  // if (this.spaces.get(identifier) != null) {
  // return this.spaces.get(identifier);
  // }
  //
  // else if (this.spaces.get(identifier) == null) {
  // space_list = this.getSpacesWithProjectInformation();
  // spaces = this.createSpaceContainer(space_list, identifier);
  //
  // this.spaces.put(identifier, spaces);
  // }
  //
  // else {
  // throw new Exception("Unknown Space: " + identifier + ". Method DataHandler::getSpace.");
  // }
  //
  // return spaces;
  //
  // }

/*
  public BeanItemContainer<DatasetBean> getDatasets(String id, String type) throws Exception {

    List<DataSet> dataset_list = null;

    if (this.datasetMap.get(id) != null) {
      return this.datasetMap.get(id);
    } else {
      switch (type) {
        case "space":
          dataset_list = this.openBisClient.getDataSetsOfSpaceByIdentifier(id);
          break;
        case "project":
          dataset_list = this.openBisClient.getDataSetsOfProjectByIdentifier(id);
          break;
        case "experiment":
          Experiment tmp_exp = this.openBisClient.getExperimentByCode(id);
          dataset_list = this.openBisClient.getDataSetsOfExperiment(tmp_exp.getPermId());
          break;
        case "sample":
          Sample sample = this.openBisClient.getSampleByIdentifier(id);
          dataset_list = this.openBisClient.getDataSetsOfSampleByIdentifier(sample.getIdentifier());
          break;
        default:
          throw new Exception("Unknown datatype: " + type);
      }
      BeanItemContainer<DatasetBean> datasets = this.createDatasetContainer(dataset_list, id);
      this.datasetMap.put(id, datasets);
      return datasets;
    }
  }

  public BeanItemContainer<SampleBean> getSamples(String id, String type) throws Exception {

    List<Sample> sample_list = null;

    if (this.sampleMap.get(id) != null) {
      return this.sampleMap.get(id);
    } else {
      switch (type) {
        case "space":
          sample_list = this.openBisClient.getSamplesofSpace(id);
          break;
        case "project":
          sample_list = this.openBisClient.getSamplesOfProject(id);
          break;
        case "experiment":
          sample_list = this.openBisClient.getSamplesofExperiment(id);
          break;
        default:
          throw new Exception("Unknown datatype: " + type);
      }
      BeanItemContainer<SampleBean> samples = this.createSampleContainer(sample_list, id);
      this.sampleMap.put(id, samples);
      return samples;
    }
  }

  public BeanItemContainer<ExperimentBean> getExperiments(String id, String type) throws Exception {

    List<Experiment> experiment_list = null;

    if (this.experimentMap.get(id) != null) {
      return this.experimentMap.get(id);
    } else {
      switch (type) {
        case "space":
          experiment_list = this.openBisClient.getExperimentsOfSpace(id);
          break;
        case "project":
          experiment_list = this.openBisClient.getExperimentsOfProjectByIdentifier(id);
          break;
        default:
          throw new Exception("Unknown datatype: " + type);
      }
      BeanItemContainer<ExperimentBean> experiments =
          this.createExperimentContainer(experiment_list, id);
      this.experimentMap.put(id, experiments);
      return experiments;
    }

  }
*/
  
  /**
   * Method to get Bean from either openbis identifier or openbis object.
   * Checks if corresponding bean is already stored in datahandler map.
   * 
   * @param 
   * @return 
   */
  public ProjectBean getProject(Object proj) {
    Project project;
    ProjectBean newProjectBean;
    System.out.println(proj);
    System.out.println(this.projectMap);

    if (proj instanceof Project) {
      project = (Project) proj;
      newProjectBean = this.createProjectBean(project);
    }
    else {
      if (this.projectMap.get((String) proj) != null) {
        System.out.println("taking it from the map");
        newProjectBean = this.projectMap.get(proj);
      } else {      
        project = this.openBisClient.getProjectByIdentifier((String) proj);
        newProjectBean = this.createProjectBean(project);
      }
    }
    this.projectMap.put(newProjectBean.getId(), newProjectBean);
    return newProjectBean;
  }
  
  /**
   * Method to get Bean from either openbis identifier or openbis object.
   * Checks if corresponding bean is already stored in datahandler map.
   * 
   * @param 
   * @return 
   */
  public ExperimentBean getExperiment(Object exp) {
    Experiment experiment;
    ExperimentBean newExperimentBean;

    if (exp instanceof Experiment) {
      experiment = (Experiment) exp;
      newExperimentBean = this.createExperimentBean(experiment);
      }
    
    else {
      if (this.experimentMap.get((String) exp) != null) {
        newExperimentBean = this.experimentMap.get(exp);
      } else {
        experiment = this.openBisClient.getExperimentById((String) exp);
        newExperimentBean = this.createExperimentBean(experiment);
      }
    }
    
    this.experimentMap.put(newExperimentBean.getId(), newExperimentBean);
    return newExperimentBean;
  }
  
  /**
   * Method to get Bean from either openbis identifier or openbis object.
   * Checks if corresponding bean is already stored in datahandler map.
   * 
   * @param 
   * @return 
   */
  public SampleBean getSample(Object samp) {
    Sample sample;
    SampleBean newSampleBean;

    if (samp instanceof Sample) {
      sample = (Sample) samp;
      newSampleBean = this.createSampleBean(sample);
    }

    else {
      if (this.sampleMap.get((String) samp) != null) {
        newSampleBean = this.sampleMap.get(samp);
      } else {
        sample = this.openBisClient.getSampleByIdentifier((String) samp);
        newSampleBean = this.createSampleBean(sample);
      }
    }
    this.sampleMap.put(newSampleBean.getId(), newSampleBean);
    return newSampleBean;
  }
  
  /**
   * Method to get Bean from either openbis identifier or openbis object.
   * Checks if corresponding bean is already stored in datahandler map.
   * 
   * @param 
   * @return 
   */
  public DatasetBean getDataset(Object ds) {
    DataSet dataset;
    DatasetBean newDatasetBean;

    if (ds instanceof DataSet) {
      dataset = (DataSet) ds;
      newDatasetBean = this.createDatasetBean(dataset);
    }

    else {
      if (this.datasetMap.get((String) ds) != null) {
        newDatasetBean = this.datasetMap.get(ds);
      } else {
        dataset = this.openBisClient.facade.getDataSet((String) ds);
        newDatasetBean = this.createDatasetBean(dataset);
      }
    }
    this.datasetMap.put(newDatasetBean.getCode(), newDatasetBean);
    return newDatasetBean;
  }
  
  
  /**
   * Returns all users of a Space.
   * 
   * @param spaceCode code of the openBIS space
   * @return set of user names as string
   */
  private Set<String> getSpaceMembers(String spaceCode) {
    List<SpaceWithProjectsAndRoleAssignments> spaces = this.getSpacesWithProjectInformation();
    for (SpaceWithProjectsAndRoleAssignments space : spaces) {
      if (space.getCode().equals(spaceCode)) {
        return space.getUsers();
      }
    }
    return null;
  }

  /*
  public Container getProjectInformation(String id) throws Exception {
    if (experimentMap.get(id) != null)
      return experimentMap.get(id);
    else {
      return getExperiments(id, "project");
    }
  }

  public Container getExperimentInformation(String id) throws Exception {
    if (sampleMap.containsKey(id)) {
      return sampleMap.get(id);
    } else {
      return getSamples(id, "experiment");
    }
    */
    // ExperimentInformation ret = new ExperimentInformation();
    // try {
    // // TODO check for source of nullpointer exception !
    // // seems like first the project id gets here
    // Experiment exp = this.openBisClient.getExperimentByCode(id);
    // // ret.identifier = exp.getIdentifier();
    // ret.experimentType = this.openBisClient.openBIScodeToString(exp.getExperimentTypeCode());
    // ret.samples = this.getSamples(id, "experiment");
    // ret.numberOfSamples = ret.samples.size();
    // List<DataSet> datasets = this.openBisClient.getDataSetsOfExperiment(exp.getPermId());
    // ret.numberOfDatasets = datasets.size();
    // StringBuilder lce = new StringBuilder();
    // StringBuilder lcs = new StringBuilder();
    // ret.lastChangedDataset = new Date(0, 0, 0);
    // this.lastDatasetRegistered(datasets, ret.lastChangedDataset, lce, lcs);
    // ret.lastChangedSample = lcs.toString();
    //
    // // TODO TEST
    // // We want to get all properties for metadata changes, not only those with values
    //
    // Map<String, String> assignedProperties = exp.getProperties();
    // List<PropertyType> completeProperties =
    // openBisClient.listPropertiesForType(openBisClient.getExperimentTypeByString(exp
    // .getExperimentTypeCode()));
    //
    // Map<String, String> properties = new HashMap<String, String>();
    // Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();
    //
    // for (PropertyType p : completeProperties) {
    //
    // if (p.getDataType().toString().equals("CONTROLLEDVOCABULARY")) {
    // controlledVocabularies
    // .put(p.getCode(), openBisClient.listVocabularyTermsForProperty(p));
    // }
    //
    // if (assignedProperties.keySet().contains(p.getCode())) {
    // properties.put(p.getCode(), assignedProperties.get(p.getCode()));
    // } else {
    // properties.put(p.getCode(), "");
    // }
    // }
    //
    // ret.properties = properties;
    // ret.controlledVocabularies = controlledVocabularies;
    //
    // // Map<String,String> typeLabels =
    // //
    // this.openBisClient.getLabelsofProperties(this.openBisClient.getExperimentTypeByString(exp.getExperimentTypeCode()));
    //
    // String propertiesHeader = "Properties \n <ul>";
    // String propertiesBottom = "";
    //
    // Iterator<Entry<String, String>> it = ret.properties.entrySet().iterator();
    // while (it.hasNext()) {
    // Entry<String, String> pairs = it.next();
    // if (pairs.getValue().equals("")) {
    // continue;
    // } else if (pairs.getKey().equals("Q_PERSONS")) {
    // continue;
    // } else {
    // // propertiesBottom += "<li><b>" + (typeLabels.get(pairs.getKey()) + ":</b> " +
    // // pairs.getValue() + "</li>");
    // propertiesBottom +=
    // "<li><b>"
    // + (this.openBisClient.openBIScodeToString(pairs.getKey().toString()) + ":</b> "
    // + pairs.getValue() + "</li>");
    // }
    // }
    // propertiesBottom += "</ul>";
    //
    // ret.propertiesFormattedString = propertiesHeader + propertiesBottom;
    //
    // this.experimentInformations.put(id, ret);
    // } catch (Exception e) {
    // e.printStackTrace();
    // ret = null;
    // }
    // return ret;
    // }
  //}

 // public Container getSampleInformation(String id) throws Exception {
   // if (datasetMap.containsKey(id)) {
    //  return datasetMap.get(id);
    //} else {
    //  return getDatasets(id, "sample");
      // SampleInformation ret = new SampleInformation();
      // Sample samp = this.openBisClient.getSampleByIdentifier(id);
      //
      // // watch out ! sample type is not the openBIS sample type anymore after this call.
      // ret.sampleType = this.openBisClient.openBIScodeToString(samp.getSampleTypeCode());
      // try {
      // ret.datasets = this.getDatasets(id, "sample");
      //
      // ret.numberOfDatasets = ret.datasets.size();
      //
      // List<DataSet> datasets =
      // this.openBisClient.getDataSetsOfSampleByIdentifier(samp.getIdentifier());
      // ret.numberOfDatasets = datasets.size();
      //
      // ret.parents = new HashMap<String, String>();
      //
      // List<Sample> parents = this.openBisClient.facade.listSamplesOfSample(samp.getPermId());
      // for (Sample s : parents) {
      // ret.parents.put(s.getIdentifier(),
      // this.openBisClient.openBIScodeToString(s.getSampleTypeCode()));
      // }
      //
      // StringBuilder lce = new StringBuilder();
      // StringBuilder lcs = new StringBuilder();
      // ret.lastChangedDataset = new Date(0, 0, 0);
      //
      // this.lastDatasetRegistered(datasets, ret.lastChangedDataset, lce, lcs);
      //
      // ret.properties = samp.getProperties();
      //
      // Map<String, String> typeLabels =
      // this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(samp
      // .getSampleTypeCode()));
      //
      //
      //
      // // String propertiesHeader = "Properties \n <ul>";
      // String propertiesBottom = "<ul> ";
      // String xmlPropertiesBottom = "<ul> ";
      //
      // Iterator it = ret.properties.entrySet().iterator();
      // while (it.hasNext()) {
      // Map.Entry pairs = (Map.Entry) it.next();
      // if (pairs.getKey().equals("Q_PROPERTIES")) {
      // Parser xmlParser = new Parser();
      // JAXBElement<Qproperties> xmlProperties =
      // xmlParser.parseXMLString(pairs.getValue().toString());
      // Map<String, String> xmlPropertiesMap = xmlParser.getMap(xmlProperties);
      //
      // Iterator itProperties = xmlPropertiesMap.entrySet().iterator();
      // while (itProperties.hasNext()) {
      // Map.Entry pairsProperties = (Map.Entry) itProperties.next();
      //
      // xmlPropertiesBottom +=
      // "<li><b>"
      // + (pairsProperties.getKey() + ":</b> " + pairsProperties.getValue() + "</li>");
      // }
      // } else {
      // propertiesBottom +=
      // "<li><b>"
      // + (typeLabels.get(pairs.getKey()) + ":</b> " + pairs.getValue() + "</li>");
      // }
      // }
      // propertiesBottom += "</ul>";
      //
      // ret.propertiesFormattedString = propertiesBottom;
      // ret.xmlPropertiesFormattedString = xmlPropertiesBottom;
      //
      // String parentsHeader = "Sample(s) derived from this sample: ";
      // String parentsBottom = "<ul>";
      //
      // if (ret.parents.isEmpty()) {
      // ret.parentsFormattedString = parentsHeader += "None";
      //
      // } else {
      // Iterator parentsIt = ret.parents.entrySet().iterator();
      // while (parentsIt.hasNext()) {
      // Map.Entry pairs = (Map.Entry) parentsIt.next();
      // parentsBottom += "<li><b>" + pairs.getKey() + "</b> (" + pairs.getValue() + ") </li>";
      // }
      // parentsBottom += "</ul>";
      // ret.parentsFormattedString = parentsHeader + parentsBottom;
      // }
      //
      // this.sampleInformations.put(id, ret);
      //
      // } catch (Exception e) {
      // e.printStackTrace();
      // ret = null;
      // }
      // return ret;
 //   }
//  }

  /**
   * checks which of the datasets in the given list is the oldest and writes that into the last tree
   * parameters Note: lastModifiedDate, lastModifiedExperiment, lastModifiedSample will be modified.
   * if lastModifiedSample, lastModifiedExperiment have value N/A datasets have no registration
   * dates Params should not be null
   * 
   * @param datasets List of datasets that will be compared
   * @param lastModifiedDate will contain the last modified date
   * @param lastModifiedExperiment will contain experiment identifier, which contains last
   *        registered dataset
   * @param lastModifiedSample will contain last sample identifier, which contains last registered
   *        dataset, or null if dataset does not belong to a sample.
   */
  public void lastDatasetRegistered(List<DataSet> datasets, Date lastModifiedDate,
      StringBuilder lastModifiedExperiment, StringBuilder lastModifiedSample) {
    String exp = "N/A";
    String samp = "N/A";
    for (DataSet dataset : datasets) {
      Date date = dataset.getRegistrationDate();

      if (date.after(lastModifiedDate)) {
        samp = dataset.getSampleIdentifierOrNull();
        if (samp == null) {
          samp = "N/A";
        }
        exp = dataset.getExperimentIdentifier();
        lastModifiedDate.setTime(date.getTime());
        break;
      }
    }
    lastModifiedExperiment.append(exp);
    lastModifiedSample.append(samp);
  }

  // public void reset() {
  // // this.spaces = new HashMap<String,IndexedContainer>();
  // // this.projects = new HashMap<String,IndexedContainer>();
  // // this.experiments = new HashMap<String,IndexedContainer>();
  // // this.samples = new HashMap<String,IndexedContainer>();
  // this.space_to_datasets = new HashMap<String, HierarchicalContainer>();
  // }


  /**
   * returns an empty Container if identifier is not a valid openbis space identifier. Else returns
   * some space informations.
   * 
   * @param spaces
   * @param identifier
   * @return
   */
  // private SpaceInformation createSpaceContainer(List<SpaceWithProjectsAndRoleAssignments> spaces,
  // String identifier) {
  // SpaceWithProjectsAndRoleAssignments tmp_space = null;
  // for (SpaceWithProjectsAndRoleAssignments space : spaces) {
  // if (space.getCode().equals(identifier)) {
  // tmp_space = space;
  // break;
  // }
  // }
  // if (tmp_space == null) {
  // System.out.println(String.format(
  // "space %s does not seem to exist! In DataHandler::createSpaceContainer", identifier));
  // return null;
  // }
  // SpaceInformation spaceInformation = new SpaceInformation();
  // IndexedContainer space_container = new IndexedContainer();
  //
  // space_container.addContainerProperty("Project", String.class, "");
  // space_container.addContainerProperty("Description", String.class, "");
  // space_container.addContainerProperty("Progress", ProgressBar.class, "");
  //
  //
  // // List<Project> projects = this.openBisClient.getProjectsofSpace(id);
  // int number_of_samples = 0;
  // List<Project> projects = tmp_space.getProjects();
  // int number_of_projects = projects.size();// projects.size();
  // int number_of_experiments = 0;
  // int number_of_datasets = 0;
  // String lastModifiedExperiment = "N/A";
  // String lastModifiedSample = "N/A";
  // Date lastModifiedDate = new Date(0, 0, 0);
  //
  // number_of_experiments = this.openBisClient.getExperimentsOfSpace(identifier).size();//
  // this.openBisClient.openbisInfoService.listExperiments(this.openBisClient.getSessionToken(),
  // // projects,
  // // null);
  // List<Sample> samplesOfSpace = this.openBisClient.getSamplesofSpace(identifier);//
  // this.openBisClient.facade.listSamplesForProjects(tmp_list_str);
  // number_of_samples += samplesOfSpace.size();
  // List<DataSet> datasets = this.openBisClient.getDataSetsOfSpaceByIdentifier(identifier); //
  // this.openBisClient.facade.listDataSetsForExperiments(tmp_experiment_identifier_lis);
  // number_of_datasets = datasets.size();
  //
  // StringBuilder lce = new StringBuilder();
  // StringBuilder lcs = new StringBuilder();
  // this.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
  // lastModifiedExperiment = lce.toString();
  // lastModifiedSample = lcs.toString();
  //
  // spaceInformation.numberOfProjects = number_of_projects;
  // spaceInformation.numberOfExperiments = number_of_experiments;
  // spaceInformation.numberOfSamples = number_of_samples;
  // spaceInformation.numberOfDatasets = number_of_datasets;
  // spaceInformation.lastChangedDataset = lastModifiedDate;
  // spaceInformation.lastChangedSample = lastModifiedSample;
  // spaceInformation.lastChangedExperiment = lastModifiedExperiment;
  //
  // spaceInformation.members = removeQBiCStaffFromMemberSet(tmp_space.getUsers());
  //
  // for (Project p : projects) {
  // Object new_s = space_container.addItem();
  // space_container.getContainerProperty(new_s, "Project").setValue(p.getCode());
  // space_container.getContainerProperty(new_s, "Description").setValue(p.getDescription());
  // space_container.getContainerProperty(new_s, "Progress").setValue(
  // new ProgressBar(this.openBisClient.computeProjectStatus(p)));
  // }
  // spaceInformation.projects = space_container;
  //
  // return spaceInformation;
  // }

  /**
   * This method filters out qbic staff and other unnecessary space members TODO: this method might
   * be better of as not being part of the DataHandler...and not hardcoded
   * 
   * @param users a set of all space users or members
   * @return a new set which exculdes qbic staff and functional members
   */
  private Set<String> removeQBiCStaffFromMemberSet(Set<String> users) {
    // TODO there is probably a method to get users of the QBIC group out of openBIS
    Set<String> ret = new LinkedHashSet<String>(users);
    ret.remove("iiswo01"); // QBiC Staff
    ret.remove("iisfr01"); // QBiC Staff
    ret.remove("kxmsn01"); // QBiC Staff
    ret.remove("zxmbf02"); // QBiC Staff
    ret.remove("qeana10"); // functional user
    ret.remove("etlserver"); // OpenBIS user
    ret.remove("admin"); // OpenBIS user
    ret.remove("QBIC"); // OpenBIS user
    ret.remove("sauron");
    // ret.remove("babysauron");
    return ret;
  }
  
  
  /**
   * Method create ProjectBean for project object
   * 
   * @param Project project
   * @return ProjectBean for corresponding project
   */
  private ProjectBean createProjectBean(Project project) {
    
    ProjectBean newProjectBean = new ProjectBean();
    
    List<Experiment> experiments = this.openBisClient.getExperimentsOfProjectByIdentifier(project.getIdentifier());
    
    ProgressBar progressBar = new ProgressBar();
    progressBar.setValue(this.openBisClient.computeProjectStatus(project));

    Date registrationDate = project.getRegistrationDetails().getRegistrationDate();
    
    newProjectBean.setId(project.getIdentifier());
    newProjectBean.setCode(project.getCode());
    newProjectBean.setDescription(project.getDescription());
    newProjectBean.setRegistrationDate(registrationDate);
    newProjectBean.setProgress(progressBar);
    newProjectBean.setRegistrator(project.getRegistrationDetails().getUserId());
    newProjectBean.setContact(project.getRegistrationDetails().getUserEmail());

    BeanItemContainer<ExperimentBean> experimentBeans = new BeanItemContainer<ExperimentBean>(ExperimentBean.class);
    
    for (Experiment experiment: experiments) {
      experimentBeans.addBean(this.getExperiment(experiment));
    }

    newProjectBean.setExperiments(experimentBeans);
    newProjectBean.setMembers(this.openBisClient.getSpaceMembers(project.getSpaceCode()));

    return newProjectBean;
  }
  
  
  /**
   * Method to create ExperimentBean for experiment object
   * 
   * @param Experiment experiment
   * @return ExperimentBean for corresponding experiment
   */
  private ExperimentBean createExperimentBean(Experiment experiment){
    
    ExperimentBean newExperimentBean = new ExperimentBean();
    List<Sample> samples = this.openBisClient.getSamplesofExperiment(experiment.getIdentifier());    

    String status = "";
    
    // Get all properties for metadata changing
    List<PropertyType> completeProperties =
        this.openBisClient.listPropertiesForType(this.openBisClient.getExperimentTypeByString(experiment
            .getExperimentTypeCode()));

    Map<String, String> assignedProperties = experiment.getProperties();
    Map<String, List<String>> controlledVocabularies = new HashMap<String, List<String>>();
    Map<String, String> properties = new HashMap<String, String>();

    if (assignedProperties.keySet().contains("Q_CURRENT_STATUS")) {
      status = assignedProperties.get("Q_CURRENT_STATUS");
    }
    
    System.out.println("hjere");
    
    for (PropertyType p : completeProperties) {
      
      //TODO no hardcoding

      if (p instanceof ControlledVocabularyPropertyType) {
        controlledVocabularies.put(p.getCode(), openBisClient.listVocabularyTermsForProperty(p));
      }

      if (assignedProperties.keySet().contains(p.getCode())) {
        properties.put(p.getCode(), assignedProperties.get(p.getCode()));
      } else {
        properties.put(p.getCode(), "");
      }
    }

    Map<String, String> typeLabels =
        this.openBisClient.getLabelsofProperties(this.openBisClient
            .getExperimentTypeByString(experiment.getExperimentTypeCode()));

    Image statusColor = new Image(status, this.setExperimentStatusColor(status));
    statusColor.setWidth("15px");
    statusColor.setHeight("15px");
    
    newExperimentBean.setId(experiment.getIdentifier());
    newExperimentBean.setCode(experiment.getCode());
    newExperimentBean.setType(experiment.getExperimentTypeCode());
    newExperimentBean.setStatus(statusColor);
    newExperimentBean.setRegistrator(experiment.getRegistrationDetails().getUserId());
    newExperimentBean.setRegistrationDate(experiment.getRegistrationDetails().getRegistrationDate());
    newExperimentBean.setProperties(properties);
    newExperimentBean.setControlledVocabularies(controlledVocabularies);
    newExperimentBean.setTypeLabels(typeLabels);
    
    //TODO do we want to have that ? (last Changed)
    newExperimentBean.setLastChangedSample(null);
    newExperimentBean.setLastChangedSample(null);
    
    System.out.println("Creating sample Beans");
    // Create sample Beans (or fetch them) for samples of experiment
    BeanItemContainer<SampleBean> sampleBeans = new BeanItemContainer<SampleBean>(SampleBean.class);
    int test = 0;
    for (Sample sample: samples) {
      test += 1;
      sampleBeans.addBean(this.getSample(sample));
      System.out.println(test);

    }
    newExperimentBean.setSamples(sampleBeans);
   
    return newExperimentBean;  
  }
  
  
  /**
   * Method to create SampleBean for sample object
   * 
   * @param Sample sample
   * @return SampleBean for corresponding object
   */
  private SampleBean createSampleBean(Sample sample){
    
    SampleBean newSampleBean = new SampleBean();
        
    Map<String, String> properties = sample.getProperties();
    
    newSampleBean.setId(sample.getIdentifier());
    newSampleBean.setCode(sample.getCode());
    newSampleBean.setType(sample.getSampleTypeCode());
    newSampleBean.setProperties(properties);
    newSampleBean.setParents(this.openBisClient.getParents(sample.getCode()));
    
    BeanItemContainer<DatasetBean> datasetBeans = new BeanItemContainer<DatasetBean>(DatasetBean.class);
    List<DataSet> datasets = this.openBisClient.getDataSetsOfSampleByIdentifier(sample.getIdentifier());
    
    Date lastModifiedDate = new Date();
    
    for (DataSet dataset: datasets) {
      datasetBeans.addBean(this.getDataset(dataset));      
      Date date = dataset.getRegistrationDate();
      if (date.after(lastModifiedDate)) {
        lastModifiedDate.setTime(date.getTime());
        break;
      }
    }
    
    newSampleBean.setDatasets(datasetBeans);
    newSampleBean.setLastChangedDataset(lastModifiedDate);
    
    Map<String, String> typeLabels = this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(sample.getSampleTypeCode()));
    newSampleBean.setTypeLabels(typeLabels);
    
    return newSampleBean; 
  }
  
  
  /**
   * Method to create DatasetBean for dataset object
   * 
   * @param Dataset dataset
   * @return DatasetBean for corresponding object
   */
  private DatasetBean createDatasetBean(DataSet dataset){
    
    DatasetBean newDatasetBean = new DatasetBean();
        
    
    newDatasetBean.setCode(dataset.getCode());
    // Whats the Name ?
    newDatasetBean.setName(dataset.tryGetInternalPathInDataStore());
    newDatasetBean.setType(dataset.getDataSetTypeCode());
    //TODO 
    //newDatasetBean.setProject(dataset.);
   // newDatasetBean.setExperiment(this.getExperiment(dataset.getExperimentIdentifier()));
    //newDatasetBean.setSample(this.getSample(dataset.getSampleIdentifierOrNull()));
    
    //TODO 
    //newDatasetBean.setRegistrator(registrator);
    newDatasetBean.setRegistrationDate(dataset.getRegistrationDate());
    //TODO 
    //newDatasetBean.setDirectory(dataset.);
    newDatasetBean.setParent(null);
    newDatasetBean.setRoot(null);
    newDatasetBean.setRoot(null);
    newDatasetBean.setSelected(false);
    
    //TODO
    //this.fileSize = fileSize;
    //this.humanReadableFileSize = humanReadableFileSize;
    //this.dssPath = dssPath;

    return newDatasetBean;  
  }

  /*
  @SuppressWarnings("unchecked")
  private BeanItemContainer<ProjectBean> createProjectContainer(List<Project> projs, String spaceID)
      throws Exception {

    BeanItemContainer<ProjectBean> res = new BeanItemContainer<ProjectBean>(ProjectBean.class);

    // project_container.addContainerProperty("Description", String.class, null);
    // project_container.addContainerProperty("Space", String.class, null);
    // project_container.addContainerProperty("Registration Date", Timestamp.class, null);
    // project_container.addContainerProperty("Registrator", String.class, null);
    // project_container.addContainerProperty("Progress", ProgressBar.class, null);
    SpaceBean space = new SpaceBean();
    space.setId(spaceID); // TODO do we need more space information at this point?
    for (Project p : projs) {
      ProgressBar progressBar = new ProgressBar();
      progressBar.setValue(this.openBisClient.computeProjectStatus(p));
      Date date = p.getRegistrationDetails().getRegistrationDate();
      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);
      ProjectBean b =
          new ProjectBean(p.getIdentifier(), p.getCode(), p.getDescription(), space,
              getExperiments(p.getIdentifier(), "project"), progressBar, ts, p
                  .getRegistrationDetails().getUserId(), p.getRegistrationDetails().getUserEmail(),
              (List<String>) getSpaceMembers(spaceID), datasetMap.get(p.getIdentifier()) != null);
      res.addBean(b);
      // Object new_p = project_container.addItem();
      //
      // String code = p.getCode();
      // String desc = p.getDescription();
      //
      // String space = code.split("/")[1];
      //
      // String registrator = p.getRegistrationDetails().getUserId();
      //

      //

      //
      // project_container.getContainerProperty(new_p, "Space").setValue(space);
      // project_container.getContainerProperty(new_p, "Description").setValue(desc);
      // project_container.getContainerProperty(new_p, "Registration Date").setValue(ts);
      // project_container.getContainerProperty(new_p, "Registerator").setValue(registrator);
      // project_container.getContainerProperty(new_p, "Progress").setValue(progressBar);
    }

    return res;
  }

*/
  /*
  @SuppressWarnings("unchecked")
  private BeanItemContainer<ExperimentBean> createExperimentContainer(List<Experiment> exps,
      String projID) {
    
    BeanItemContainer<ExperimentBean> res = new BeanItemContainer<ExperimentBean>(ExperimentBean.class);

    // project_container.addContainerProperty("Description", String.class, null);
    // project_container.addContainerProperty("Space", String.class, null);
    // project_container.addContainerProperty("Registration Date", Timestamp.class, null);
    // project_container.addContainerProperty("Registrator", String.class, null);
    // project_container.addContainerProperty("Progress", ProgressBar.class, null);
    
    
    
    
    

    IndexedContainer experiment_container = new IndexedContainer();

    experiment_container.addContainerProperty("Experiment", String.class, null);
    experiment_container.addContainerProperty("Experiment Type", String.class, null);
    experiment_container.addContainerProperty("Registration Date", Timestamp.class, null);
    experiment_container.addContainerProperty("Registrator", String.class, null);
    experiment_container.addContainerProperty("Status", Image.class, null);
    // experiment_container.addContainerProperty("Properties", Map.class, null);
    ProjectBean space = new ProjectBean();
    space.setId(spaceID); // TODO do we need more space information at this point?
    for (Experiment e : exps) {
      ProgressBar progressBar = new ProgressBar();
      progressBar.setValue(this.openBisClient.computeProjectStatus(p));
      Date date = p.getRegistrationDetails().getRegistrationDate();
      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);
      ExperimentBean e = new ExperimentBean(id, code, type, status, registrator, project, registrationDate, samples,
          lastChangedSample, lastChangedDataset, properties, controlledVocabularies)
      ProjectBean b =
          new ProjectBean(p.getIdentifier(), p.getCode(), p.getDescription(), space,
              getExperiments(p.getIdentifier(), "project"), progressBar, ts, p
                  .getRegistrationDetails().getUserId(), p.getRegistrationDetails().getUserEmail(),
              (List<String>) getSpaceMembers(spaceID), datasetMap.get(p.getIdentifier()) != null);
      res.addBean(b);




    for (Experiment e : exps) {
      Object new_ds = experiment_container.addItem();

      String type = this.openBisClient.openBIScodeToString(e.getExperimentTypeCode());

      Map<String, String> properties = e.getProperties();

      String status = "";

      if (properties.keySet().contains("Q_CURRENT_STATUS")) {
        status = properties.get("Q_CURRENT_STATUS");
      }

      Date date = e.getRegistrationDetails().getRegistrationDate();
      String registrator = e.getRegistrationDetails().getUserId();

      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);
      experiment_container.getContainerProperty(new_ds, "Experiment").setValue(e.getCode());
      experiment_container.getContainerProperty(new_ds, "Experiment Type").setValue(type);
      experiment_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      experiment_container.getContainerProperty(new_ds, "Registrator").setValue(registrator);

      Image statusColor = new Image(status, this.setExperimentStatusColor(status));
      statusColor.setWidth("15px");
      statusColor.setHeight("15px");
      experiment_container.getContainerProperty(new_ds, "Status").setValue(statusColor);
      // experiment_container.getContainerProperty(new_ds,
      // "Properties").setValue(e.getProperties());
    }

    return experiment_container;
  }
*/
  
  /*
  @SuppressWarnings("unchecked")
  private BeanItemContainer<SampleBean> createSampleContainer(List<Sample> samples, String id) {

    IndexedContainer sample_container = new IndexedContainer();
    sample_container.addContainerProperty("Sample", String.class, null);
    sample_container.addContainerProperty("Description", String.class, null);
    sample_container.addContainerProperty("Sample Type", String.class, null);
    sample_container.addContainerProperty("Registration Date", Timestamp.class, null);
    // sample_container.addContainerProperty("Species", String.class, null);

    for (Sample s : samples) {
      Object new_ds = sample_container.addItem();

      String type = this.openBisClient.openBIScodeToString(s.getSampleTypeCode());

      Date date = s.getRegistrationDetails().getRegistrationDate();
      Map<String, String> properties = s.getProperties();
      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);
      sample_container.getContainerProperty(new_ds, "Sample").setValue(s.getCode());
      // sample_container.getContainerProperty(new_ds,
      // "Description").setValue(properties.get("SAMPLE_CLASS"));
      sample_container.getContainerProperty(new_ds, "Description").setValue(
          properties.get("Q_SECONDARY_NAME"));
      sample_container.getContainerProperty(new_ds, "Sample Type").setValue(type);
      sample_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      // sample_container.getContainerProperty(new_ds,
      // "Species").setValue(properties.get("SPECIES"));
    }

    return sample_container;
  }
*/
  /*
  private BeanItemContainer<DatasetBean> createDatasetContainer(List<DataSet> datasets, String id) {

    HierarchicalContainer dataset_container = new HierarchicalContainer();

    dataset_container.addContainerProperty("Select", CheckBox.class, null);
    dataset_container.addContainerProperty("Project", String.class, null);
    dataset_container.addContainerProperty("Sample", String.class, null);
    dataset_container.addContainerProperty("Sample Type", String.class, null);
    dataset_container.addContainerProperty("File Name", String.class, null);
    dataset_container.addContainerProperty("File Type", String.class, null);
    dataset_container.addContainerProperty("Dataset Type", String.class, null);
    dataset_container.addContainerProperty("Registration Date", Timestamp.class, null);
    dataset_container.addContainerProperty("Validated", Boolean.class, null);
    dataset_container.addContainerProperty("File Size", String.class, null);
    dataset_container.addContainerProperty("file_size_bytes", Long.class, null);
    dataset_container.addContainerProperty("dl_link", String.class, null);
    dataset_container.addContainerProperty("CODE", String.class, null);

    for (DataSet d : datasets) {
      String identifier = d.getSampleIdentifierOrNull();
      Sample sampleObject = this.openBisClient.getSampleByIdentifier(identifier);
      String sample = sampleObject.getCode();
      String sampleType = this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode();
      Project projectObject =
          this.openBisClient.getProjectOfExperimentByIdentifier(sampleObject
              .getExperimentIdentifierOrNull());
      String project = projectObject.getCode();
      // String code = d.getSampleIdentifierOrNull();
      // String sample = code.split("/")[2];
      // String project = sample.substring(0, 5);
      Date date = d.getRegistrationDate();

      SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
      String dateString = sd.format(date);
      Timestamp ts = Timestamp.valueOf(dateString);

      FileInfoDssDTO[] filelist = d.listFiles("original", true);

      // recursive test
      registerDatasetInTable(d, filelist, dataset_container, project, sample, ts, sampleType, null);

    }

    return dataset_container;
  }
*/
  
  public void registerDatasetInTable(DataSet d, FileInfoDssDTO[] filelist,
      HierarchicalContainer dataset_container, String project, String sample, Timestamp ts,
      String sampleType, Object parent) {
    if (filelist[0].isDirectory()) {

      Object new_ds = dataset_container.addItem();

      String folderPath = filelist[0].getPathInDataSet();
      FileInfoDssDTO[] subList = d.listFiles(folderPath, false);

      dataset_container.setChildrenAllowed(new_ds, true);
      String download_link = filelist[0].getPathInDataSet();
      String[] splitted_link = download_link.split("/");
      String file_name = download_link.split("/")[splitted_link.length - 1];
      // System.out.println(file_name);

      dataset_container.getContainerProperty(new_ds, "Select").setValue(new CheckBox());

      dataset_container.getContainerProperty(new_ds, "Project").setValue(project);
      dataset_container.getContainerProperty(new_ds, "Sample").setValue(sample);
      dataset_container.getContainerProperty(new_ds, "Sample Type").setValue(
          this.openBisClient.getSampleByIdentifier(sample).getSampleTypeCode());
      dataset_container.getContainerProperty(new_ds, "File Name").setValue(file_name);
      dataset_container.getContainerProperty(new_ds, "File Type").setValue("Folder");
      dataset_container.getContainerProperty(new_ds, "Dataset Type").setValue("-");
      dataset_container.getContainerProperty(new_ds, "Registration Date").setValue(ts);
      dataset_container.getContainerProperty(new_ds, "Validated").setValue(true);
      dataset_container.getContainerProperty(new_ds, "dl_link").setValue(
          d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
      dataset_container.getContainerProperty(new_ds, "CODE").setValue(d.getCode());
      dataset_container.getContainerProperty(new_ds, "file_size_bytes").setValue(
          filelist[0].getFileSize());

      // System.out.println("Now it should be a folder: " + filelist[0].getPathInDataSet());

      if (parent != null) {
        dataset_container.setParent(new_ds, parent);
      }

      for (FileInfoDssDTO file : subList) {
        FileInfoDssDTO[] childList = {file};
        registerDatasetInTable(d, childList, dataset_container, project, sample, ts, sampleType,
            new_ds);
      }

    } else {
      // System.out.println("Now it should be a file: " + filelist[0].getPathInDataSet());

      Object new_file = dataset_container.addItem();
      dataset_container.setChildrenAllowed(new_file, false);
      String download_link = filelist[0].getPathInDataSet();
      String[] splitted_link = download_link.split("/");
      String file_name = download_link.split("/")[splitted_link.length - 1];
      // String file_name = download_link.split("/")[1];
      String fileSize = DashboardUtil.humanReadableByteCount(filelist[0].getFileSize(), true);

      dataset_container.getContainerProperty(new_file, "Select").setValue(new CheckBox());
      dataset_container.getContainerProperty(new_file, "Project").setValue(project);
      dataset_container.getContainerProperty(new_file, "Sample").setValue(sample);
      dataset_container.getContainerProperty(new_file, "Sample Type").setValue(sampleType);
      dataset_container.getContainerProperty(new_file, "File Name").setValue(file_name);
      dataset_container.getContainerProperty(new_file, "File Type")
          .setValue(d.getDataSetTypeCode());
      dataset_container.getContainerProperty(new_file, "Dataset Type").setValue(
          d.getDataSetTypeCode());
      dataset_container.getContainerProperty(new_file, "Registration Date").setValue(ts);
      dataset_container.getContainerProperty(new_file, "Validated").setValue(true);
      dataset_container.getContainerProperty(new_file, "File Size").setValue(fileSize);
      dataset_container.getContainerProperty(new_file, "dl_link").setValue(
          d.getDataSetDss().tryGetInternalPathInDataStore() + "/" + filelist[0].getPathInDataSet());
      dataset_container.getContainerProperty(new_file, "CODE").setValue(d.getCode());
      dataset_container.getContainerProperty(new_file, "file_size_bytes").setValue(
          filelist[0].getFileSize());
      if (parent != null) {
        dataset_container.setParent(new_file, parent);
      }
    }
  }

  /**
   * Function to fill tree container and collect statistical information of spaces. Should replace
   * the two functions and be somewhat faster. Still not pretty. Needs work
   * 
   * @param tc HierarchicalContainer for the Tree
   * @param userName Screenname of the Liferay User
   * @return SpaceInformation object
   */
  // public SpaceInformation initTreeAndHomeInfo(HierarchicalContainer tc, String userName) {
  //
  // List<SpaceWithProjectsAndRoleAssignments> space_list = this.getSpacesWithProjectInformation();
  //
  // // Initialization of Tree Container
  // tc.addContainerProperty("identifier", String.class, "N/A");
  // tc.addContainerProperty("type", String.class, "N/A");
  // tc.addContainerProperty("project", String.class, "N/A");
  // tc.addContainerProperty("caption", String.class, "N/A");
  //
  //
  // // Initialization of Home Information
  // SpaceInformation homeInformation = new SpaceInformation();
  // IndexedContainer space_container = new IndexedContainer();
  // space_container.addContainerProperty("Project", String.class, "");
  // space_container.addContainerProperty("Description", String.class, "");
  // space_container.addContainerProperty("Contains datasets", String.class, "");
  // int number_of_projects = 0;
  // int number_of_experiments = 0;
  // int number_of_samples = 0;
  // int number_of_datasets = 0;
  // String lastModifiedExperiment = "N/A";
  // String lastModifiedSample = "N/A";
  // Date lastModifiedDate = new Date(0, 0, 0);
  // for (SpaceWithProjectsAndRoleAssignments s : space_list) {
  // if (s.getUsers().contains(userName)) {
  // String space_name = s.getCode();
  //
  // // TODO does this work for everyone? should it? empty container would be the aim, probably
  // if (space_name.equals("QBIC_USER_SPACE")) {
  // fillPersonsContainer(space_name);
  // }
  //
  // List<Project> projects = s.getProjects();
  // number_of_projects += projects.size();
  // List<String> project_identifiers_tmp = new ArrayList<String>();
  // for (Project project : projects) {
  //
  // String project_name = project.getCode();
  // if (tc.containsId(project_name)) {
  // project_name = project.getIdentifier();
  // }
  // Object new_s = space_container.addItem();
  // space_container.getContainerProperty(new_s, "Project").setValue(project_name);
  //
  // // Project descriptions can be long; truncate the string to provide a brief preview
  // String desc = project.getDescription();
  //
  // if (desc != null && desc.length() > 0) {
  // desc = desc.substring(0, Math.min(desc.length(), 100));
  // if (desc.length() == 100) {
  // desc += "...";
  // }
  // }
  // space_container.getContainerProperty(new_s, "Description").setValue(desc);
  //
  // // System.out.println("|--Project: " + project_name);
  // tc.addItem(project_name);
  //
  // tc.getContainerProperty(project_name, "type").setValue("project");
  // tc.getContainerProperty(project_name, "identifier").setValue(project_name);
  // tc.getContainerProperty(project_name, "project").setValue(project_name);
  // tc.getContainerProperty(project_name, "caption").setValue(project_name);
  //
  // List<Project> tmp_list = new ArrayList<Project>();
  // tmp_list.add(project);
  // List<Experiment> experiments =
  // this.openBisClient.getOpenbisInfoService().listExperiments(
  // this.openBisClient.getSessionToken(), tmp_list, null);
  //
  // // Add number of experiments for every project
  // number_of_experiments += experiments.size();
  //
  // List<String> experiment_identifiers = new ArrayList<String>();
  //
  // for (Experiment experiment : experiments) {
  // experiment_identifiers.add(experiment.getIdentifier());
  // String experiment_name = experiment.getCode();
  // if (tc.containsId(experiment_name)) {
  // experiment_name = experiment.getIdentifier();
  // }
  // // System.out.println(" |--Experiment: " + experiment_name);
  // tc.addItem(experiment_name);
  // tc.setParent(experiment_name, project_name);
  // tc.getContainerProperty(experiment_name, "type").setValue("experiment");
  // tc.getContainerProperty(experiment_name, "identifier").setValue(experiment_name);
  // tc.getContainerProperty(experiment_name, "project").setValue(project_name);
  // tc.getContainerProperty(experiment_name, "caption").setValue(
  // String.format("%s (%s)",
  // this.openBisClient.openBIScodeToString(experiment.getExperimentTypeCode()),
  // experiment_name));
  //
  // tc.setChildrenAllowed(experiment_name, false);
  // }
  // if (experiment_identifiers.size() > 0
  // && this.openBisClient.getFacade().listDataSetsForExperiments(experiment_identifiers)
  // .size() > 0) {
  // space_container.getContainerProperty(new_s, "Contains datasets").setValue("yes");
  // } else {
  // space_container.getContainerProperty(new_s, "Contains datasets").setValue("no");
  // }
  // }
  // List<Sample> samplesOfSpace = new ArrayList<Sample>();
  // if (project_identifiers_tmp.size() > 0) {
  // samplesOfSpace =
  // this.openBisClient.getFacade().listSamplesForProjects(project_identifiers_tmp);
  // } else {
  // samplesOfSpace = this.openBisClient.getSamplesofSpace(space_name); // TODO code or
  // // identifier
  // // needed?
  // }
  // number_of_samples += samplesOfSpace.size();
  // List<String> sample_identifiers_tmp = new ArrayList<String>();
  // for (Sample sa : samplesOfSpace) {
  // sample_identifiers_tmp.add(sa.getIdentifier());
  // }
  // List<DataSet> datasets = new ArrayList<DataSet>();
  // if (sample_identifiers_tmp.size() > 0) {
  // datasets = this.openBisClient.getFacade().listDataSetsForSamples(sample_identifiers_tmp);
  // }
  // number_of_datasets += datasets.size();
  // StringBuilder lce = new StringBuilder();
  // StringBuilder lcs = new StringBuilder();
  // this.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
  // String tmplastModifiedExperiment = lce.toString();
  // String tmplastModifiedSample = lcs.toString();
  // if (!tmplastModifiedSample.equals("N/A")) {
  // lastModifiedExperiment = tmplastModifiedExperiment;
  // lastModifiedSample = tmplastModifiedSample;
  // }
  // }
  // }
  // homeInformation.numberOfProjects = number_of_projects;
  // homeInformation.numberOfExperiments = number_of_experiments;
  // homeInformation.numberOfSamples = number_of_samples;
  // homeInformation.numberOfDatasets = number_of_datasets;
  // homeInformation.lastChangedDataset = lastModifiedDate;
  // homeInformation.lastChangedSample = lastModifiedSample;
  // homeInformation.lastChangedExperiment = lastModifiedExperiment;
  // homeInformation.projects = space_container;
  //
  // return homeInformation;
  // }

  /**
   * Creates a Map of project statuses fulfilled, keyed by their meaning. For this, different steps
   * in the project flow are checked by looking at experiment types and data registered
   * 
   * @param project openBIS project
   * @return
   */
  public Map<String, Integer> computeProjectStatuses(ProjectBean projectBean) {

    //Project p = this.openBisClient.getProjectByCode(projectId);
    Map<String, Integer> res = new HashMap<String, Integer>();
    BeanItemContainer<ExperimentBean> cont = projectBean.getExperiments();
        
    // project was planned (otherwise it would hopefully not exist :) )
    res.put("Project Planned", 1);
    // design is pre-registered to the test sample level
    int prereg = 0;
    for (ExperimentBean bean : cont.getItemIds()) {
      String type = bean.getType();
      if (type.equals(this.openBisClient.openBIScodeToString(ExperimentType.Q_SAMPLE_PREPARATION
          .toString()))) {
        prereg = 1;
        break;
      }
    }
    res.put("Experimental Design registered", prereg);
    // data is uploaded
    //TODO fix that
    //if (datasetMap.get(p.getIdentifier()) != null)
    //  res.put("Data Registered", 1);
   // else
      res.put("Data Registered", 0);
    return res;
  }

  public ThemeResource setExperimentStatusColor(String status) {
    ThemeResource resource = null;
    if (status.equals("FINISHED")) {
      resource = new ThemeResource("green_light.png");
    } else if (status.equals("DELAYED")) {
      resource = new ThemeResource("yellow_light.png");
    } else if (status.equals("STARTED")) {
      resource = new ThemeResource("grey_light.png");
    } else if (status.equals("FAILED")) {
      resource = new ThemeResource("red_light.png");
    } else {
      resource = new ThemeResource("red_light.png");
    }

    // image.setWidth("15px");
    // image.setHeight("15px");\
    return resource;
  }

  // public String beanContainerToString(BeanItemContainer c) {
  // String header = "";
  // for (Object o : c.getContainerPropertyIds())
  // header += o.toString() + "\t";
  // for (c.get)
  // }


  public List<Qperson> parseConnectedPeopleInformation(String xmlString) {
    PersonParser xmlParser = new PersonParser();
    List<Qperson> xmlPersons = null;
    try {
      xmlPersons = xmlParser.getPersonsFromXML(xmlString);
    } catch (JAXBException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return xmlPersons;
  }

  public void fillPersonsContainer(String spaceIdentifier) {
    List<Sample> samplesOfSpace = new ArrayList<Sample>();
    samplesOfSpace = this.openBisClient.getSamplesofSpace(spaceIdentifier);

    if (this.connectedPersons.size() == 0) {
      for (PropertyType p : this.openBisClient.listPropertiesForType(this.openBisClient
          .getSampleTypeByString(("Q_USER")))) {
        this.connectedPersons.addContainerProperty(p.getLabel(), String.class, null);
      }
      this.connectedPersons.addContainerProperty("Project", String.class, null);
    }

    for (Sample s : samplesOfSpace) {
      List<Sample> parents = this.openBisClient.getParents(s.getCode());
      Map<String, String> labelMap =
          this.openBisClient.getLabelsofProperties(this.openBisClient.getSampleTypeByString(s
              .getSampleTypeCode()));

      for (Sample parent : parents) {
        Object newPerson = this.connectedPersons.addItem();
        Iterator it = s.getProperties().entrySet().iterator();
        while (it.hasNext()) {
          Map.Entry pairs = (Map.Entry) it.next();
          this.connectedPersons.getContainerProperty(newPerson, labelMap.get(pairs.getKey()))
              .setValue(pairs.getValue());
        }
        this.connectedPersons.getContainerProperty(newPerson, "Project").setValue(
            this.openBisClient
                .getProjectOfExperimentByIdentifier(parent.getExperimentIdentifierOrNull())
                .getCode().toString());

      }
    }
  }

}
