/*
 * Created on Mar 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package edu.uoregon.tau.paraprof;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

/**
 * @author amorris
 *
 * TODO ...
 */
public class FunctionSelectorDialog extends JDialog {

    private Vector items = new Vector();
    private JList list;
    private boolean selected;
    private Object selectedObject;
    
    
    private void center(JFrame owner) {
        
      
        int centerOwnerX = owner.getX() + (owner.getWidth() / 2);
        int centerOwnerY = owner.getY() + (owner.getHeight() / 2);
        
        
        int posX = centerOwnerX-(this.getWidth()/2);
        int posY = centerOwnerY-(this.getHeight()/2);
        
        posX = Math.max(posX,0);
        posY = Math.max(posY,0);
        
        this.setLocation(posX, posY );
        
    }
    
    public boolean choose() {
        this.show();

        if (!selected)
            return false;
        
        if (list.getSelectedIndex() == 0) {
            selectedObject = null;
        } else {
            selectedObject = items.get(list.getSelectedIndex());
        }
        
        return true;
    }

    
    public FunctionSelectorDialog(JFrame owner, boolean modal, Iterator functions, Object initialSelection) {
        
        super(owner, modal);
        this.setTitle("Select a Function");
        this.setSize(600,600);
      
        center(owner);
        
        System.out.println ("initialSelection = " + initialSelection);
        int selectedIndex = 0;
        
        items.add("   <none>");
        int index = 1;
        for (Iterator it = functions; it.hasNext();) {
            Object object = it.next();
            if (object == initialSelection) {
                selectedIndex = index;
            }
            items.add(object);
            index++;
        }

        list = new JList(items);
        list.setSelectedIndex(selectedIndex);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sp = new JScrollPane(list);
        
        
        
        Container panel = this.getContentPane();
        
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        //gbc.insets = new Insets(1, 1, 1, 1);

        
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JButton okButton = new JButton ("select");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    selected = true;
                    dispose();
                } catch (Exception e) {
                    ParaProfUtils.handleException(e);
                }
            }
        });
        JButton cancelButton = new JButton ("cancel");
        
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                try {
                    dispose();
                } catch (Exception e) {
                    ParaProfUtils.handleException(e);
                }
            }
        });
        
        
        
        JPanel buttonPanel = new JPanel();
//        buttonPanel.setLayout(new GridBagLayout());
//        buttonPanel.setBorder(BorderFactory.createLoweredBevelBorder());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        ParaProfUtils.addCompItem(panel, sp, gbc, 0, 0, 1, 1);
        //ParaProfUtils.addCompItem(panel, sp, gbc, 0, 0, 2, 1);

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 0;
        gbc.weighty = 0;

        ParaProfUtils.addCompItem(panel, buttonPanel, gbc, 0, 1, 1, 1);
            //ParaProfUtils.addCompItem(panel, okButton, gbc, 0, 1, 1, 1);
            //ParaProfUtils.addCompItem(panel, cancelButton, gbc, 1, 1, 1, 1);
        
    }
    
    public Object getSelectedObject() {
        return selectedObject;
    }
}
