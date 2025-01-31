package clearcontrol.timelapse.gui;

import clearcontrol.LightSheetMicroscope;
import clearcontrol.MicroscopeInterface;
import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.configurationstate.gui.ConfigurationStatePanel;
import clearcontrol.core.configuration.MachineConfiguration;
import clearcontrol.core.log.LoggingFeature;
import clearcontrol.core.variable.Variable;
import clearcontrol.gui.halcyon.MicroscopeNodeType;
import clearcontrol.gui.jfx.custom.gridpane.CustomGridPane;
import clearcontrol.instructions.InstructionInterface;
import clearcontrol.timelapse.Timelapse;
import clearcontrol.timelapse.TimelapseInterface;
import clearcontrol.timelapse.io.ProgramReader;
import clearcontrol.timelapse.io.ProgramWriter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * Lightsheet Timelapse toolbar
 *
 * @author royer
 */
public class TimelapseToolbar extends TimelapseToolbarBasics implements LoggingFeature
{
  private TimelapseInterface mTimelapse;

  ScrollPane mPropertiesScrollPane;
  ListView<InstructionInterface> mCurrentProgramScheduleListView;

  private final File mProgramTemplateDirectory = MachineConfiguration.get().getFolder("ProgramTemplates");

  /**
   * Instanciates a lightsheet timelapse toolbar.
   *
   * @param pTimelapse timelapse device
   */
  public TimelapseToolbar(Timelapse pTimelapse)
  {
    super(pTimelapse);
    mTimelapse = pTimelapse;

    this.setAlignment(Pos.TOP_LEFT);

    setPrefSize(400, 200);

    int[] lPercent = new int[]{10, 40, 40, 10};
    for (int j : lPercent) {
      ColumnConstraints lColumnConstraints = new ColumnConstraints();
      lColumnConstraints.setPercentWidth(j);
      getColumnConstraints().add(lColumnConstraints);
    }

    {
      Separator lSeparator = new Separator();
      lSeparator.setOrientation(Orientation.HORIZONTAL);
      GridPane.setColumnSpan(lSeparator, 4);
      add(lSeparator, 0, mRow);
      mRow++;
    }

    {
      int lRow = 0;
      CustomGridPane lSchedulerChecklistGridPane = new CustomGridPane();

      TitledPane lTitledPane = new TitledPane("Schedule", lSchedulerChecklistGridPane);
      lTitledPane.setAnimated(false);
      lTitledPane.setExpanded(true);
      GridPane.setColumnSpan(lTitledPane, 4);
      add(lTitledPane, 0, mRow);
      mRow++;

      {
        Label lLabel = new Label("Current program");
        lSchedulerChecklistGridPane.add(lLabel, 0, lRow);
        lRow++;
      }

      ArrayList<InstructionInterface> lProgramList = pTimelapse.getCurrentProgram();
      mCurrentProgramScheduleListView = new ListView<>();
      mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
      refreshPropertiesScrollPane();
      mCurrentProgramScheduleListView.setMinHeight(300);
      mCurrentProgramScheduleListView.setMinWidth(450);

      mCurrentProgramScheduleListView.setOnMouseClicked(mouseEvent -> {
        if (mouseEvent.getClickCount() > 0)
        {
          refreshPropertiesScrollPane();
        }
      });

      lSchedulerChecklistGridPane.add(mCurrentProgramScheduleListView, 0, lRow, 1, 9);

      {
        Button lMoveUpButton = new Button("^");
        lMoveUpButton.setTooltip(new Tooltip("Move up"));
        lMoveUpButton.setMinWidth(35);
        lMoveUpButton.setMinHeight(35);
        lMoveUpButton.setOnAction((e) ->
        {
          int i = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (i > 0)
          {
            InstructionInterface lInstructionInterface = lProgramList.get(i);
            lProgramList.remove(i);
            lProgramList.add(i - 1, lInstructionInterface);
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
            refreshPropertiesScrollPane();
          }
        });
        lSchedulerChecklistGridPane.add(lMoveUpButton, 1, lRow);
        lRow++;
      }

      {
        Button lMoveDownButton = new Button("v");
        lMoveDownButton.setTooltip(new Tooltip("Move down"));
        lMoveDownButton.setMinWidth(35);
        lMoveDownButton.setMinHeight(35);
        lMoveDownButton.setOnAction((e) ->
        {
          int count = 0;
          int i = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (i >= 0 && i < lProgramList.size() - 1)
          {
            InstructionInterface lInstructionInterface = lProgramList.get(i);
            lProgramList.remove(i);
            lProgramList.add(i + 1, lInstructionInterface);
            mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
            refreshPropertiesScrollPane();
          }
        });
        lSchedulerChecklistGridPane.add(lMoveDownButton, 1, lRow);
        lRow++;
      }

      {
        Button lMinusButton = new Button("-");
        lMinusButton.setTooltip(new Tooltip("Remove"));
        lMinusButton.setMinWidth(35);
        lMinusButton.setMinHeight(35);
        lMinusButton.setOnAction((e) ->
        {
          int count = 0;
          int lSelectedIndex = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          for (int i : mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndices().sorted())
          {
            lProgramList.remove(i - count);
            count++;
          }
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
          mCurrentProgramScheduleListView.getSelectionModel().select(lSelectedIndex);
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lMinusButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lMinusButton, 1, lRow);
        lRow++;
      }

      {
        Button lUnselectButton = new Button("[]");
        lUnselectButton.setTooltip(new Tooltip("Unselect"));
        lUnselectButton.setMinWidth(35);
        lUnselectButton.setMinHeight(35);
        lUnselectButton.setOnAction((e) ->
        {
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
          mCurrentProgramScheduleListView.getSelectionModel().select(-1);
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lUnselectButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lUnselectButton, 1, lRow);
        lRow++;
      }

      {
        Button lCloneButton = new Button("++");
        lCloneButton.setTooltip(new Tooltip("Clone"));
        lCloneButton.setMinWidth(35);
        lCloneButton.setMinHeight(35);
        lCloneButton.setOnAction((e) ->
        {
          int lSelectedIndex = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
          if (lSelectedIndex > -1)
          {
            lProgramList.add(lSelectedIndex, lProgramList.get(lSelectedIndex).copy());
          }
          mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
          refreshPropertiesScrollPane();
        });
        GridPane.setValignment(lCloneButton, VPos.BOTTOM);
        lSchedulerChecklistGridPane.add(lCloneButton, 1, lRow);
        lRow++;
      }

      lRow = 10;
      {
        ComboBox lExistingProgramTemplates;
        {
          // load
          lExistingProgramTemplates = new ComboBox(listExistingSchedulerTemplateFiles());
          lSchedulerChecklistGridPane.add(lExistingProgramTemplates, 0, lRow);

          Button lLoadScheduleTemplateBytton = new Button("Load");
          lLoadScheduleTemplateBytton.setMaxWidth(Double.MAX_VALUE);
          lLoadScheduleTemplateBytton.setOnAction((e) ->
          {
            try
            {
              mTimelapse.getCurrentProgram().clear();
              new ProgramReader(lProgramList, (LightSheetMicroscope) mTimelapse.getMicroscope(), getFile(lExistingProgramTemplates.getValue().toString())).read();
              mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
              refreshPropertiesScrollPane();
            } catch (Exception e1)
            {
              e1.printStackTrace();
            }
          });

          lSchedulerChecklistGridPane.add(lLoadScheduleTemplateBytton, 1, lRow, 2, 1);
          lRow++;

        }

        {
          // save
          Variable<String> lFileNameVariable = new Variable<String>("filename", "Program");

          TextField lFileNameTextField = new TextField(lFileNameVariable.get());
          lFileNameTextField.setMaxWidth(Double.MAX_VALUE);
          lFileNameTextField.textProperty().addListener((obs, o, n) ->
          {
            String lName = n.trim();
            if (!lName.isEmpty()) lFileNameVariable.set(lName);
          });
          lSchedulerChecklistGridPane.add(lFileNameTextField, 0, lRow);

          Button lSaveProgramButton = new Button("Save");
          lSaveProgramButton.setAlignment(Pos.CENTER);
          lSaveProgramButton.setMaxWidth(Double.MAX_VALUE);
          lSaveProgramButton.setOnAction((e) ->
          {
            try
            {
              new ProgramWriter(mTimelapse.getCurrentProgram(), getFile(lFileNameVariable.get())).write();
              lExistingProgramTemplates.setItems(listExistingSchedulerTemplateFiles());
            } catch (Exception e1)
            {
              e1.printStackTrace();
            }
          });
          GridPane.setColumnSpan(lSaveProgramButton, 1);
          lSchedulerChecklistGridPane.add(lSaveProgramButton, 1, lRow, 2, 1);
          lRow++;
        }

      }

      String[] lFilters = {"Acquisition:", "Adaptation:", "Adaptive optics:", "Filter wheel:", "Fusion:",

              "IO:", "Laser:", "Memory:", "Post-processing:", "Smart:",

              "Timing:", "Visualisation:"};

      Node[] lIcons = {MicroscopeNodeType.Acquisition.getIcon(), MicroscopeNodeType.AdaptiveOptics.getIcon(), MicroscopeNodeType.AdaptiveOptics.getIcon(), MicroscopeNodeType.FilterWheel.getIcon(), MicroscopeNodeType.Scripting.getIcon(),

              MicroscopeNodeType.Laser.getIcon(), MicroscopeNodeType.Scripting.getIcon(), MicroscopeNodeType.Scripting.getIcon(), MicroscopeNodeType.Scripting.getIcon(), MicroscopeNodeType.Scripting.getIcon(),

              MicroscopeNodeType.Scripting.getIcon(), MicroscopeNodeType.StackDisplay3D.getIcon()};

      lRow = 0;
      // properties panel
      {

        Label lLabel = new Label("Properties");
        lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
        lRow++;

        mPropertiesScrollPane = new ScrollPane();
        mPropertiesScrollPane.setMinHeight(150);
        mPropertiesScrollPane.setMaxHeight(150);
        mPropertiesScrollPane.setMaxHeight(450);
        lSchedulerChecklistGridPane.add(mPropertiesScrollPane, 2, lRow, 2, 5);
        lRow++;

      }

      lRow = 7;
      {
        Label lLabel = new Label("Add instruction");
        lSchedulerChecklistGridPane.add(lLabel, 2, lRow, 2, 1);
        lRow++;

        TreeItem<String> rootItem = buildInstructionTree(mTimelapse, lFilters, "", lIcons);
        TreeView<String> tree = new TreeView<>(rootItem);

        Label lSearchLabel = new Label("Search");
        lSchedulerChecklistGridPane.add(lSearchLabel, 2, lRow);

        TextField lSearchField = new TextField();
        lSchedulerChecklistGridPane.add(lSearchField, 3, lRow);
        lSchedulerChecklistGridPane.setOnKeyReleased((e) ->
        {
          info("keyreleased");
          tree.setRoot(buildInstructionTree(mTimelapse, lFilters, lSearchField.getText(), lIcons));
        });
        lRow++;

        tree.setMinHeight(150);
        tree.setMinWidth(450);

        tree.setOnMouseClicked(mouseEvent -> {
          if (mouseEvent.getClickCount() == 2)
          {
            TreeItem<String> item = tree.getSelectionModel().getSelectedItem();
            System.out.println("Selected Text : " + item.getValue());
            if (item.getParent() != null && item.getParent().getValue().compareTo("Instructions") != 0)
            {
              int lSelectedIndexInMainList = mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex();
              if (lSelectedIndexInMainList < 0) lSelectedIndexInMainList = lProgramList.size();
              lProgramList.add(lSelectedIndexInMainList, mTimelapse.getListOfAvailableInstructions(item.getParent().getValue() + ":" + item.getValue()).get(0).copy());
              mCurrentProgramScheduleListView.setItems(FXCollections.observableArrayList(lProgramList));
              if (mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex() > -1)
              {
                mCurrentProgramScheduleListView.getSelectionModel().select(mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex() - 1);
              }
              refreshPropertiesScrollPane();
            }
          }
        });

        StackPane lStackPane = new StackPane();
        lStackPane.getChildren().add(tree);

        lSchedulerChecklistGridPane.add(lStackPane, 2, lRow, 2, 1);
        // lRow+=5;

      }

    }

    CustomGridPane lAdvancedOptionsGridPane = buildAdvancedOptionsGripPane();

    int lRow = lAdvancedOptionsGridPane.getLastUsedRow();

    {
      MicroscopeInterface lMicroscopeInterface = mTimelapse.getMicroscope();
      AdaptiveEngine lAdaptiveEngine = (AdaptiveEngine) lMicroscopeInterface.getDevice(AdaptiveEngine.class, 0);

      if (lAdaptiveEngine != null)
      {
        int lNumberOfLightSheets = 1;
        if (lMicroscopeInterface instanceof LightSheetMicroscope)
        {
          lNumberOfLightSheets = ((LightSheetMicroscope) lMicroscopeInterface).getNumberOfLightSheets();
        }

        ConfigurationStatePanel lConfigurationStatePanel = new ConfigurationStatePanel(lAdaptiveEngine.getModuleList(), lNumberOfLightSheets);

        TitledPane lTitledPane = new TitledPane("Adaptation state", lConfigurationStatePanel);
        lTitledPane.setAnimated(false);
        lTitledPane.setExpanded(false);
        GridPane.setColumnSpan(lTitledPane, 4);
        add(lTitledPane, 0, mRow);
        mRow++;
      }
    }

  }

  private void refreshPropertiesScrollPane()
  {
    ArrayList<InstructionInterface> lSchedulerList = mTimelapse.getCurrentProgram();
    if (mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex() > -1)
    {
      InstructionInterface lInstruction = lSchedulerList.get(mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex());
      System.out.println("Selected: " + lSchedulerList.get(mCurrentProgramScheduleListView.getSelectionModel().getSelectedIndex()));
      try
      {
        Class<?> lInstructionClass = lInstruction.getClass();
        String lInstructionClassName = lInstructionClass.getSimpleName();
        String lInstructionPanelClassName = lInstructionClass.getPackage().getName() + ".gui." + lInstructionClassName + "Panel";
        info("Searching for class %s as panel for calibration module %s \n", lInstructionPanelClassName, lInstructionClassName);
        Class<?> lClass = Class.forName(lInstructionPanelClassName);
        Constructor<?> lConstructor = lClass.getConstructor(lInstruction.getClass());
        Node lPanel = (Node) lConstructor.newInstance(lInstruction);

        mPropertiesScrollPane.setContent(lPanel);
      } catch (ClassNotFoundException e)
      {
        warning("Cannot find panel for module %s \n", lInstruction.getClass().getSimpleName());
        // e.printStackTrace();
        mPropertiesScrollPane.setContent(null);
      } catch (Throwable e)
      {
        e.printStackTrace();
      }
    }
  }

  private TreeItem<String> buildInstructionTree(TimelapseInterface pTimelapse, String[] lFilters, String pSearchFilter, Node[] lIcons)
  {
    TreeItem<String> rootItem = new TreeItem<>("Instructions", MicroscopeNodeType.Other.getIcon());
    rootItem.setExpanded(true);
    for (int i = 0; i < lFilters.length; i++)
    {
      ArrayList<InstructionInterface> lAvailableSchedulersList = pTimelapse.getListOfAvailableInstructions(lFilters[i], pSearchFilter);
      if (lAvailableSchedulersList.size() > 0)
      {

        TreeItem<String> item = new TreeItem<>(lFilters[i].replace(":", ""), lIcons[i]);
        item.setExpanded(pSearchFilter.length() > 0);
        rootItem.getChildren().add(item);

        for (InstructionInterface lInstructionInterface : lAvailableSchedulersList)
        {
          TreeItem<String> schedulerItem = new TreeItem<>(lInstructionInterface.getName().replace(lFilters[i], ""));
          item.getChildren().add(schedulerItem);
        }
      }
    }
    return rootItem;
  }

  private ObservableList<String> listExistingSchedulerTemplateFiles()
  {
    ArrayList<String> filenames = getScheduleTemplateNames();
    return FXCollections.observableArrayList(filenames);
  }

  private File getFile(String pName)
  {
    return new File(mProgramTemplateDirectory, pName + ".txt");
  }

  ArrayList<String> mExistingTemplateFileList = new ArrayList<>();

  private ArrayList<String> getScheduleTemplateNames()
  {
    File folder = mProgramTemplateDirectory;

    mExistingTemplateFileList.clear();
    for (File file : folder.listFiles())
    {
      if (!file.isDirectory() && file.getAbsolutePath().endsWith(".txt"))
      {
        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);

        mExistingTemplateFileList.add(fileName);
      }
    }

    return mExistingTemplateFileList;
  }

}
