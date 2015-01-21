package de.uni_tuebingen.qbic.qbicmainportlet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.portlet.PortletSession;

import ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.theme.ThemeDisplay;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WrappedPortletSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import de.uni_tuebingen.qbic.main.ConfigurationManager;
import de.uni_tuebingen.qbic.main.ConfigurationManagerFactory;
import de.uni_tuebingen.qbic.main.LiferayAndVaadinUtils;

/*
 * in portal-ext.properties the following settings must be set, in order to work
 * com.liferay.portal.servlet.filters.etag.ETagFilter=false
 * com.liferay.portal.servlet.filters.header.HeaderFilter=false
 */
@SuppressWarnings("serial")
@Theme("qbicmainportlet")
@Widgetset("de.uni_tuebingen.qbic.qbicmainportlet.QbicmainportletWidgetset")

public class QbicmainportletUI extends UI {

  private OpenBisClient openBisConnection;
  private VerticalLayout mainLayout;

  @Override
  protected void init(VaadinRequest request) {
    if (LiferayAndVaadinUtils.getUser() == null) {
      buildNoUserLogin();    
    } else {
      DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      System.out.println("QbicNavigator\nUser logged in: " + LiferayAndVaadinUtils.getUser().getScreenName()
          + " at " + dateFormat.format(new Date()) + " UTC.");
      initConnection();
      initSessionAttributes();
      initProgressBarAndThreading(request);
      //buildMainLayout(); 
    }
  }

  private void buildNoUserLogin() {
    ExternalResource resource = new ExternalResource("mailto:info@qbic.uni-tuebingen.de");
    Link mailToQbicLink = new Link("", resource);
    mailToQbicLink.setIcon(new ThemeResource("mail9.png"));
    mainLayout = new VerticalLayout();
    mainLayout.setMargin(false);
    ThemeDisplay themedisplay =
        (ThemeDisplay) VaadinService.getCurrentRequest().getAttribute(WebKeys.THEME_DISPLAY);
    Link loginPortalLink = new Link("", new ExternalResource(themedisplay.getURLSignIn()));
    loginPortalLink.setIcon(new ThemeResource("lock12.png"));

    VerticalLayout signIn = new VerticalLayout();



    signIn.addComponent(new Label("<h3>Sign in to manage your projects and access your data:</h3>",
        ContentMode.HTML));
    signIn.addComponent(loginPortalLink);
    signIn.setStyleName("no-user-login");
    VerticalLayout contact = new VerticalLayout();
    contact.addComponent(new Label(
        "<h3>If you are interested in doing projects get in contact:</h3>", ContentMode.HTML));
    contact.addComponent(mailToQbicLink);
    contact.setStyleName("no-user-login");

    HorizontalLayout notSignedInLayout = new HorizontalLayout();
    Label expandingGap1 = new Label();
    expandingGap1.setWidth("100%");
    notSignedInLayout.addComponent(expandingGap1);
    notSignedInLayout.addComponent(signIn);

    notSignedInLayout.addComponent(contact);
    notSignedInLayout.setExpandRatio(expandingGap1, 0.16f);
    notSignedInLayout.setExpandRatio(signIn, 0.36f);

    notSignedInLayout.setExpandRatio(contact, 0.36f);

    notSignedInLayout.setWidth("100%");
    notSignedInLayout.setSpacing(true);
    setContent(notSignedInLayout);
  }
  private ProgressBar progress;
  private Label status;
  private Button button;
  protected void initProgressBarAndThreading(VaadinRequest request){
    HorizontalLayout barbar = new HorizontalLayout();
    final VerticalLayout layout = new VerticalLayout();
    layout.addComponent(barbar);
    this.setContent(layout);
    
    // Create the indicator, disabled until progress is started
    progress = new ProgressBar(new Float(0.0));
    progress.setEnabled(false);
    barbar.addComponent(progress);
            
    status = new Label("not running");
    barbar.addComponent(status);
    final HierarchicalContainer tc = new HierarchicalContainer();
    final SpaceInformation homeInformation = new SpaceInformation(); 
    // A button to start progress
    button = new Button("Update Database");
    layout.addComponent(button);
 // Clicking the button creates and runs a work thread
    button.addClickListener(new Button.ClickListener() {
        public void buttonClick(ClickEvent event) {
          final RunnableFillsContainer th = new RunnableFillsContainer(tc, homeInformation, LiferayAndVaadinUtils.getUser().getScreenName());
            final Thread thread = new Thread(th);
            Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread th, Throwable ex) {
                    System.out.println("Uncaught exception: " + ex);
                    layout.addComponent(new Label("Uncaught exception: " + ex));
                }
            };
            thread.setUncaughtExceptionHandler(h);
            thread.start();

            // Enable polling and set frequency to 0.5 seconds
            UI.getCurrent().setPollInterval(500);

            // Disable the button until the work is done
            progress.setEnabled(true);
            button.setEnabled(false);

            status.setValue("running...");
        }
    });
    
  }

  class RunnableFillsContainer implements Runnable{
    // Volatile because read in another thread in access()
    volatile double current = 0.0;
    private HierarchicalContainer tc;
    private SpaceInformation homeViewInformation;
    private String userName;
    public RunnableFillsContainer(HierarchicalContainer tc, SpaceInformation homeInformation, String user){
      this.tc = tc;
      this.homeViewInformation = homeInformation;
      this.userName = user;
    }
    
    @Override
    public void run() {
      long startTime = System.nanoTime();

      DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");

      status.setValue("Connecting to database.");
        List<SpaceWithProjectsAndRoleAssignments> space_list = dh.getSpace_list();

        // Initialization of Tree Container
        tc.addContainerProperty("identifier", String.class, "N/A");
        tc.addContainerProperty("type", String.class, "N/A");
        tc.addContainerProperty("project", String.class, "N/A");
        tc.addContainerProperty("caption", String.class, "N/A");

        // Initialization of Home Information
        IndexedContainer space_container = new IndexedContainer();
        space_container.addContainerProperty("Project", String.class, "");
        space_container.addContainerProperty("Description", String.class, "");
        space_container.addContainerProperty("Contains datasets", String.class, "");
        int number_of_projects = 0;
        int i = 0;
        int all = space_list.size();
        int number_of_experiments = 0;
        int number_of_samples = 0;
        int number_of_datasets = 0;
        String lastModifiedExperiment = "N/A";
        String lastModifiedSample = "N/A";
        Date lastModifiedDate = new Date(0, 0, 0);
        for (SpaceWithProjectsAndRoleAssignments s : space_list) {
          if (s.getUsers().contains(userName)) {
            String space_name = s.getCode();

            // TODO does this work for everyone? should it? empty container would be the aim, probably
            if (space_name.equals("QBIC_USER_SPACE")) {
              dh.fillPersonsContainer(space_name);
            }

            List<Project> projects = s.getProjects();
            number_of_projects += projects.size();
            List<String> project_identifiers_tmp = new ArrayList<String>();
            for (Project project : projects) {

              String project_name = project.getCode();
              if (tc.containsId(project_name)) {
                project_name = project.getIdentifier();
              }
              Object new_s = space_container.addItem();
              space_container.getContainerProperty(new_s, "Project").setValue(project_name);

              // Project descriptions can be long; truncate the string to provide a brief preview
              String desc = project.getDescription();

              if (desc != null && desc.length() > 0) {
                desc = desc.substring(0, Math.min(desc.length(), 100));
                if (desc.length() == 100) {
                  desc += "...";
                }
              }
              space_container.getContainerProperty(new_s, "Description").setValue(desc);

              // System.out.println("|--Project: " + project_name);
              tc.addItem(project_name);
              
              tc.getContainerProperty(project_name, "type").setValue("project");
              tc.getContainerProperty(project_name, "identifier").setValue(project_name);
              tc.getContainerProperty(project_name, "project").setValue(project_name);
              tc.getContainerProperty(project_name, "caption").setValue(project_name);

              List<Project> tmp_list = new ArrayList<Project>();
              tmp_list.add(project);
              List<Experiment> experiments = dh.listExperimentsOfProjects(tmp_list);

              // Add number of experiments for every project
              number_of_experiments += experiments.size();

              List<String> experiment_identifiers = new ArrayList<String>();

              for (Experiment experiment : experiments) {
                experiment_identifiers.add(experiment.getIdentifier());
                String experiment_name = experiment.getCode();
                if (tc.containsId(experiment_name)) {
                  experiment_name = experiment.getIdentifier();
                }
                // System.out.println(" |--Experiment: " + experiment_name);
                tc.addItem(experiment_name);
                tc.setParent(experiment_name, project_name);
                tc.getContainerProperty(experiment_name, "type").setValue("experiment");
                tc.getContainerProperty(experiment_name, "identifier").setValue(experiment_name);
                tc.getContainerProperty(experiment_name, "project").setValue(project_name);
                tc.getContainerProperty(experiment_name, "caption").setValue(String.format("%s (%s)", dh.openBIScodeToString(experiment.getExperimentTypeCode()),experiment_name));

                tc.setChildrenAllowed(experiment_name, false);
              }
              if (experiment_identifiers.size() > 0
                  && dh.listDataSetsForExperiments(experiment_identifiers).size() > 0) {
                space_container.getContainerProperty(new_s, "Contains datasets").setValue("yes");
              } else {
                space_container.getContainerProperty(new_s, "Contains datasets").setValue("no");
              }
         
            }
            List<Sample> samplesOfSpace = new ArrayList<Sample>();
            if (project_identifiers_tmp.size() > 0) {
              samplesOfSpace = dh.listSamplesForProjects(project_identifiers_tmp);
            } else {
              samplesOfSpace = dh.getSamplesofSpace(space_name); // TODO code or identifier
                                                                           // needed?
            }
            number_of_samples += samplesOfSpace.size();
            List<String> sample_identifiers_tmp = new ArrayList<String>();
            for (Sample sa : samplesOfSpace) {
              sample_identifiers_tmp.add(sa.getIdentifier());
            }
            List<DataSet> datasets = new ArrayList<DataSet>();
            if (sample_identifiers_tmp.size() > 0) {
              datasets = dh.listDataSetsForSamples(sample_identifiers_tmp);
            }
            number_of_datasets += datasets.size();
            StringBuilder lce = new StringBuilder();
            StringBuilder lcs = new StringBuilder();
            dh.lastDatasetRegistered(datasets, lastModifiedDate, lce, lcs);
            String tmplastModifiedExperiment = lce.toString();
            String tmplastModifiedSample = lcs.toString();
            if (!tmplastModifiedSample.equals("N/A")) {
              lastModifiedExperiment = tmplastModifiedExperiment;
              lastModifiedSample = tmplastModifiedSample;
            }
          }
          UI.getCurrent().access(new UpdateProgressbar(QbicmainportletUI.getCurrent().getProgressBar(),QbicmainportletUI.getCurrent().getStatusLabel(), (double)(i+1)/(double)all));
          ++i;  
        }
        homeViewInformation.numberOfProjects = number_of_projects;
        homeViewInformation.numberOfExperiments = number_of_experiments;
        homeViewInformation.numberOfSamples = number_of_samples;
        homeViewInformation.numberOfDatasets = number_of_datasets;
        homeViewInformation.lastChangedDataset = lastModifiedDate;
        homeViewInformation.lastChangedSample = lastModifiedSample;
        homeViewInformation.lastChangedExperiment = lastModifiedExperiment;
        homeViewInformation.projects = space_container;
        long endTime = System.nanoTime();
        System.out.println("Took "+((endTime - startTime)/ 1000000000.0) + " s");
        
        System.out.println("User " +userName + " has " + homeViewInformation.numberOfProjects + " projects.");
        try {
            Thread.currentThread().sleep(100); // Sleep for 100 milliseconds
        } catch (InterruptedException e) {}

        // Update the UI thread-safely
        UI.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                // Restore the state to initial
                progress.setValue(new Float(0.0));
                progress.setEnabled(false);     
                // Stop polling
                UI.getCurrent().setPollInterval(-1);
                
                QbicmainportletUI.getCurrent().buildMainLayout(tc, homeViewInformation);
                
                getButton().setEnabled(true);
                status.setValue(String.valueOf("done"));
                
            }
        });
    }
}  
  
  public ProgressBar getProgressBar(){
    return progress;
}

public Label getStatusLabel(){
    return status;
}

public Button getButton(){
    return button;
}
 
public static QbicmainportletUI getCurrent(){
  return (QbicmainportletUI) UI.getCurrent();
}

class UpdateProgressbar implements Runnable {
  ProgressBar progress;
  Label label;
  double current;
  @Override
  public void run() {
      progress.setValue(new Float(current));
      if (current < 1.0)
          status.setValue("" +
              ((int)(current*100)) + "% done, ("+ current + ")");
      else
          status.setValue("all done");
  }
  
  public UpdateProgressbar(ProgressBar progress, Label label, double current2){
      this.progress = progress;
      this.label = label;
      this.current = current2;
  }
}

  
  public void buildMainLayout( HierarchicalContainer tc, SpaceInformation homeViewInformation) {
    //HierarchicalContainer tc = new HierarchicalContainer();
    //System.out.println("Filling HierarchicalTreeContainer and preparing HomeView..");
    //long startTime = System.nanoTime();
    
    
    
    //DataHandler dh = (DataHandler) UI.getCurrent().getSession().getAttribute("datahandler");
    //User user = LiferayAndVaadinUtils.getUser();
    //SpaceInformation homeViewInformation = dh.initTreeAndHomeInfo(tc, user.getScreenName());
    
    //long endTime = System.nanoTime();
    //System.out.println("Took "+((endTime - startTime)/ 1000000000.0) + " s");
    
    //System.out.println("User " +user.getScreenName() + " has " + homeViewInformation.numberOfProjects + " projects.");
    State state = (State) UI.getCurrent().getSession().getAttribute("state");

    LevelView spaceView =
        new LevelView(new SpaceView());
    LevelView addspaceView =
        new LevelView(
            new Button(
                "I am doing nothing. But you will be able to add workspaces some day in the \"early\" future."));// new
                                                                                                             // AddSpaceView(new
                                                                                                             // Table(),
                                                                                                             // spaces));

    HomeView homeView;

    if (homeViewInformation.numberOfProjects > 0) {
      homeView =
          new HomeView(
              homeViewInformation, "Your Projects");
    } else {
      homeView =new HomeView();
    }
    LevelView maxQuantWorkflowView =
        new LevelView( new Button("maxQuantWorkflowView"));
    QcMlWorkflowView qcmlView = new QcMlWorkflowView();
    state.addObserver(qcmlView);
    LevelView qcMlWorkflowView =
        new LevelView(qcmlView);
    LevelView testRunWorkflowView =
        new LevelView( new Button(
            "testRunWorkflowView"));
    LevelView searchView = new LevelView(new SearchForUsers());

    VerticalLayout navigatorContent = new VerticalLayout();
    Navigator navigator = new Navigator(UI.getCurrent(), navigatorContent);
    navigator.addView("space", spaceView);
    navigator.addView("addspaceView", addspaceView);
    navigator.addView("datasetView", new DatasetView());
    navigator.addView(SampleView.navigateToLabel, new SampleView());
    navigator.addView("", homeView);
    
    navigator.addView(ProjectView.navigateToLabel,new ProjectView());
    navigator.addView(ExperimentView.navigateToLabel, new ExperimentView());
    navigator.addView(ChangePropertiesView.navigateToLabel, new ChangePropertiesView());
    navigator.addView("maxQuantWorkflow", maxQuantWorkflowView);
    navigator.addView("qcMlWorkflow", qcMlWorkflowView);
    navigator.addView("testRunWorkflow", testRunWorkflowView);
    navigator.addView("searchView", searchView);

  

    setNavigator(navigator);


    mainLayout = new VerticalLayout();
    mainLayout.setMargin(true);    
    
    TreeView tv = createTreeView(tc, state);
    navigator.addViewChangeListener(tv);
    HorizontalLayout treeViewAndLevelView = new HorizontalLayout();
    treeViewAndLevelView.addComponent(tv);
    
    treeViewAndLevelView.addComponent(navigatorContent);
    mainLayout.addComponent(treeViewAndLevelView);
    
    setContent(mainLayout);

    navigator.navigateTo("");
  }

  private TreeView createTreeView(HierarchicalContainer tc, State st) {
    TreeView t = new TreeView();
    t.tree.setContainerDataSource(tc);
    st.addObserver(t);
    return t;


  }
  
  public PortletSession getPortletSession(){
    UI.getCurrent().getSession().getService();
    VaadinRequest vaadinRequest= VaadinService.getCurrentRequest();
    WrappedPortletSession wrappedPortletSession = (WrappedPortletSession)vaadinRequest.getWrappedSession();
    return wrappedPortletSession.getPortletSession();
  }
  
  
  private void initSessionAttributes() {
    if (this.openBisConnection == null) {
      this.initConnection();
    }
    UI.getCurrent().getSession().setAttribute("state", new State());
    DataHandler dataHandler = new DataHandler(this.openBisConnection);
    UI.getCurrent().getSession()
        .setAttribute("datahandler", dataHandler);
    UI.getCurrent().getSession().setAttribute("qbic_download", new HashMap<String, AbstractMap.SimpleEntry<String, Long>>());
    
    
    PortletSession portletSession = ((QbicmainportletUI) UI.getCurrent()).getPortletSession();
    portletSession.setAttribute("datahandler", dataHandler, PortletSession.APPLICATION_SCOPE);
    
    portletSession.setAttribute("qbic_download", new HashMap<String, AbstractMap.SimpleEntry<String, Long>>(),
        PortletSession.APPLICATION_SCOPE);
    
  }

  private void initConnection() {
    ConfigurationManager manager = ConfigurationManagerFactory.getInstance();

    this.openBisConnection =
        new OpenBisClient(manager.getDataSourceUser(), manager.getDataSourcePassword(),
            manager.getDataSourceUrl(), true);
    addDetachListener(new DetachListener() {

      @Override
      public void detach(DetachEvent event) {
        openBisConnection.logout();
      }
    });
  }

}
