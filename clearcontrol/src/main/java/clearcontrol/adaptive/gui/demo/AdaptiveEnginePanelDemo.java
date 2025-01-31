package clearcontrol.adaptive.gui.demo;

import clearcontrol.adaptive.AdaptiveEngine;
import clearcontrol.adaptive.gui.AdaptiveEnginePanel;
import clearcontrol.adaptive.test.AdaptationTestModule;
import clearcontrol.adaptive.test.TestState;
import clearcontrol.gui.jfx.custom.visualconsole.VisualConsoleInterface.ChartType;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Simulation manager demo
 *
 * @author royer
 */
public class AdaptiveEnginePanelDemo extends Application
{

  @Override
  public void start(Stage stage)
  {
    Group root = new Group();
    Scene scene = new Scene(root, 800, 600);
    stage.setScene(scene);
    stage.setTitle(AdaptiveEnginePanelDemo.class.getSimpleName());
    // scene.setFill(Color.BLACK);

    AdaptiveEngine<TestState> lAdaptator = new AdaptiveEngine<TestState>(null, new TestState("initial state"));

    AdaptationTestModule lAdaptationModuleA = new AdaptationTestModule("A", 2, 2, 2);
    AdaptationTestModule lAdaptationModuleB = new AdaptationTestModule("B", 3);

    lAdaptator.add(lAdaptationModuleA);
    lAdaptator.add(lAdaptationModuleB);
    // lAdaptator.add(new AdaptationTestModule("C", 2, 3));

    AdaptiveEnginePanel lAdaptiveEnginePanel = new AdaptiveEnginePanel(lAdaptator);

    root.getChildren().add(lAdaptiveEnginePanel);

    lAdaptator.configureChart(lAdaptationModuleA.getName(), "test", "x", "y", ChartType.Line);

    for (int i = 0; i < 100; i++)
    {
      double x = i;
      double y = Math.cos(0.1 * x);

      lAdaptator.addPoint(lAdaptationModuleA.getName(), "test", i == 0, x, y);

      lAdaptator.addPoint(lAdaptationModuleB.getName(), "test", i == 0, x, y);

      lAdaptator.addPoint(lAdaptationModuleB.getName(), "test2", i == 0, x, y * y);

    }

    stage.show();
  }

  /**
   * Main
   *
   * @param args NA
   */
  public static void main(String[] args)
  {
    launch(args);
  }
}
