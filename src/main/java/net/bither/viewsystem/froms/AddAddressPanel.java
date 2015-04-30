package net.bither.viewsystem.froms;

import net.bither.Bither;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.delegate.IPasswordGetterDelegate;
import net.bither.fonts.AwesomeIcon;
import net.bither.languages.MessageKey;
import net.bither.utils.KeyUtil;
import net.bither.utils.LocaliserUtils;
import net.bither.viewsystem.base.Panels;
import net.bither.viewsystem.dialogs.DialogProgress;
import net.bither.xrandom.PrivateKeyUEntropyDialog;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddAddressPanel extends WizardPanel implements IPasswordGetterDelegate {
    private JSpinner spinnerCount;
    private JCheckBox xrandomCheckBox;
    private PasswordPanel.PasswordGetter passwordGetter;

    public AddAddressPanel() {
        super(MessageKey.ADD, AwesomeIcon.PLUS);

        setOkAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                generateKey();
            }
        });
        passwordGetter = new PasswordPanel.PasswordGetter(AddAddressPanel.this);

    }


    private void generateKey() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                passwordGetter.getPassword();
            }
        }).start();

    }


    @Override
    public void beforePasswordDialogShow() {

    }

    @Override
    public void afterPasswordDialogDismiss() {
        closePanel();
        final int targetCount = Integer.valueOf(spinnerCount.getValue().toString());
        final DialogProgress dialogProgress = new DialogProgress();
        if (!xrandomCheckBox.isSelected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.pack();
                            dialogProgress.setVisible(true);
                        }
                    });
                    KeyUtil.addPrivateKeyByRandomWithPassphras(null, passwordGetter.getPassword(), targetCount);
                    passwordGetter.wipe();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            dialogProgress.dispose();
                            Bither.refreshFrame();
                        }
                    });

                }
            }).start();

        } else {
            PrivateKeyUEntropyDialog uEntropyDialog = new PrivateKeyUEntropyDialog(targetCount, passwordGetter);
            uEntropyDialog.pack();
            uEntropyDialog.setVisible(true);


        }
    }

    @Override
    public void initialiseContent(JPanel panel) {

        panel.setLayout(new MigLayout(
                Panels.migXYLayout(),
                "[]", // Column constraints
                "[][][][]80[]20[]" // Row constraints
        ));
        spinnerCount = new JSpinner();
        panel.add(spinnerCount, "align center,cell 0 2 ,wrap");
        xrandomCheckBox = new JCheckBox();
        xrandomCheckBox.setSelected(true);
        xrandomCheckBox.setText(LocaliserUtils.getString("xrandom"));
        panel.add(xrandomCheckBox, "align center,cell 0 3,wrap");

        if (AddressManager.isPrivateLimit()) {
            spinnerCount.setEnabled(false);
            setOkEnabled(false);
            xrandomCheckBox.setEnabled(false);
        } else {
            Integer value = new Integer(1);
            Integer min = new Integer(1);

            Integer max = new Integer(AddressManager.canAddPrivateKeyCount());
            Integer step = new Integer(1);
            SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
            spinnerCount.setModel(model);
        }
        xrandomCheckBox.setSelected(true);

    }
}
