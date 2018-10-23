package pubui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class SystemPanel extends JPanel
{
  private JTextField[] endpoints_;
  private JTextField[] gateways_;

  public SystemPanel() throws Exception
  {
    super( new GridBagLayout() );

    endpoints_ = new JTextField[3];
    gateways_ = new JTextField[3];

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.insets = new Insets( 5, 5, 5, 5 );
    gbc.ipadx = 5;
    gbc.ipady = 5;
    gbc.anchor = GridBagConstraints.LINE_START;

    gbc.gridx = 0;
    gbc.gridy += 1;
    gbc.gridwidth = 2;
    add( new JLabel(Resources.instance().get("ServiceEndpointsLabel")), gbc );

    gbc.gridy += 1;
    gbc.gridwidth = 1;
    add( new JLabel("a. "), gbc );
    gbc.gridx += 1;
    add( endpoints_[0] = new JTextField(33), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    add( new JLabel("b. "), gbc );
    gbc.gridx += 1;
    add( endpoints_[1] = new JTextField(33), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    add( new JLabel("c. "), gbc );
    gbc.gridx += 1;
    add( endpoints_[2] = new JTextField(33), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    gbc.gridwidth = 2;
    add( new JLabel(Resources.instance().get("EndpointsLink")), gbc );

    gbc.gridy += 1;
    add( new JLabel(" "), gbc ); // spacer

    gbc.gridy += 1;
    gbc.gridwidth = 2;
    add( new JLabel(Resources.instance().get("GatewaysLabel")), gbc );

    gbc.gridy += 1;
    gbc.gridwidth = 1;
    add( new JLabel("a. "), gbc );
    gbc.gridx += 1;
    add( gateways_[0] = new JTextField(33), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    add( new JLabel("b. "), gbc );
    gbc.gridx += 1;
    add( gateways_[1] = new JTextField(33), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    add( new JLabel("c. "), gbc );
    gbc.gridx += 1;
    add( gateways_[2] = new JTextField(33), gbc );

    gbc.gridx = 0;
    gbc.gridy += 1;
    gbc.gridwidth = 2;
    add( new JLabel(Resources.instance().get("GatewaysLink")), gbc );
  }

  public String[] endpoints()
  {
    String[] result = new String[ endpoints_.length ];
    for (int ii = 0; ii < result.length; ii++)
      result[ii] = endpoints_[ii].getText();
    return result;
  }

  public String[] gateways()
  {
    String[] result = new String[ gateways_.length ];
    for (int ii = 0; ii < result.length; ii++)
      result[ii] = gateways_[ii].getText();
    return result;
  }
}
