package clearvolume.renderer.panels.demo;

import clearvolume.renderer.panels.ParametersListPanelJFrame;
import org.junit.Test;

import javax.swing.*;

public class ProcessorsListJPanelDemo
{

  @Test
  public void test() throws InterruptedException
  {
    final ParametersListPanelJFrame lParametersListPanelJFrame = new ParametersListPanelJFrame();

    lParametersListPanelJFrame.setVisible(true);

    lParametersListPanelJFrame.addPanel(getPanel(0));
    lParametersListPanelJFrame.addPanel(getPanel(1));
    lParametersListPanelJFrame.addPanel(getPanel(2));
    lParametersListPanelJFrame.addPanel(getPanel(3));
    lParametersListPanelJFrame.addPanel(getPanel(4));
    lParametersListPanelJFrame.addPanel(getPanel(5));

    while (lParametersListPanelJFrame.isVisible())
    {
      Thread.sleep(100);
    }

  }

  public JPanel getPanel(int pIndex)
  {
    final JPanel lJPanel = new JPanel();

    final JLabel lJLabel = new JLabel("This is panel %" + pIndex);

    lJPanel.add(lJLabel);

    final JButton lJButton = new JButton();

    lJPanel.add(lJButton);

    lJPanel.validate();

    return lJPanel;
  }

}
