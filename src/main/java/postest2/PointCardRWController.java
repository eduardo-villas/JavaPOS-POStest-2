package postest2;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jpos.JposException;
import jpos.PointCardRW;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PointCardRWController extends CommonController implements Initializable {

	@FXML
	@RequiredState(JposState.ENABLED)
	public Pane pane;
	@FXML
	public TextArea textToPrint;
	@FXML
	public ComboBox<String> kind;
	@FXML
	public ComboBox<String> mapMode;
	@FXML
	public ComboBox<String> rotationMode;
	@FXML
	public ComboBox<Integer> characterSet;
	@FXML
	public TextField hpos;
	@FXML
	public TextField vpos;
	@FXML
	public TextField width;
	@FXML
	public TextField height;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		service = new PointCardRW();
		RequiredStateChecker.invokeThis(this, service);
	}

	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */

	@FXML
	public void handleSetCharacterSet(ActionEvent e) {
		try {
			((PointCardRW) service).setCharacterSet(characterSet.getSelectionModel().getSelectedItem());
		} catch (JposException jpe) {
			JOptionPane.showMessageDialog(null, jpe.getMessage());
			jpe.printStackTrace();
		}
	}

	/**
	 * PointCardRW waits for a point card insertion.
	 * @param e
	 */
	@FXML
	public void handleBeginInsertion(ActionEvent e) {
		try {
			// "-1" wait as long as needed until either point card is inserted
			// or an error occurs.
			((PointCardRW) service).beginInsertion(-1);
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleEndInsertion(ActionEvent e) {
		try {
			((PointCardRW) service).endInsertion();
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	/**
	 * PointCardRW waits for a point card ejection.
	 * @param e
	 */
	@FXML
	public void handleBeginRemoval(ActionEvent e) {
		try {
			// "-1" wait as long as needed until either the point card is
			// removed or an error occurs.
			((PointCardRW) service).beginRemoval(-1);
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleEndRemoval(ActionEvent e) {
		try {
			((PointCardRW) service).endRemoval();
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	/**
	 * Called to determine whether a data sequence, possibly including one or
	 * e more escape sequences, is valid for printing, prior to calling the
	 * printWrite method.
	 * @param 
	 */ 
	@FXML
	public void handleValidateData(ActionEvent e) {
		try {
			((PointCardRW) service).validateData(textToPrint.getText());
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleAddEscSec(ActionEvent e) {
		String text = textToPrint.getText();
		String first = text.substring(0, textToPrint.getCaretPosition());
		String second = text.substring(textToPrint.getCaretPosition(), textToPrint.lengthProperty()
				.getValue());

		textToPrint.setText(first + "|" + second);
		textToPrint.positionCaret(textToPrint.getLength() - 1);
	}

	@FXML
	public void handlePrintWrite(ActionEvent e) {
		try {
			int tKind = kind.getSelectionModel().getSelectedIndex();
			if (tKind > -1) {
				((PointCardRW) service).printWrite(tKind + 1, Integer.parseInt(hpos.getText()),
						Integer.parseInt(vpos.getText()), textToPrint.getText());
			} else {
				JOptionPane.showMessageDialog(null, "Choose a Map mode.", "Map mode!",
						JOptionPane.WARNING_MESSAGE);
			}
		} catch (Exception jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleRotatePrint(ActionEvent e) {
		try {
			((PointCardRW) service).rotatePrint(PointCardRWConstantMapper
					.getConstantNumberFromString(rotationMode.getSelectionModel().getSelectedItem()));
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleCleanCard(ActionEvent e) {
		try {
			((PointCardRW) service).cleanCard();
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleCleanPrintWrite(ActionEvent e) {
		try {
			int tKind = kind.getSelectionModel().getSelectedIndex();
			if (tKind > -1) {
				((PointCardRW) service).clearPrintWrite(tKind, Integer.parseInt(hpos.getText()),
						Integer.parseInt(vpos.getText()), Integer.parseInt(width.getText()),
						Integer.parseInt(height.getText()));
			} else {
				JOptionPane.showMessageDialog(null, "Choose a Map mode.", "Map mode!",
						JOptionPane.WARNING_MESSAGE);
			}
		} catch (JposException jpe) {
			jpe.printStackTrace();
		}
	}

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((PointCardRW) service).setDeviceEnabled(true);

				setUpCharacterSet();
				setUpRotationMode();
				setUpMapMode();
			} else {
				((PointCardRW) service).setDeviceEnabled(false);
			}
			RequiredStateChecker.invokeThis(this, service);
		} catch (JposException je) {
			System.err.println("PointCardRWPanel: CheckBoxListener: Jpos Exception" + je);
		}
	}

	@Override
	@FXML
	public void handleOCE(ActionEvent e) {
		super.handleOCE(e);
		deviceEnabled.setSelected(true);
		handleDeviceEnable(e);
	}

	/**
	 * Shows statistics of device if they are supported by the device
	 */
	@Override
	@FXML
	public void handleInfo(ActionEvent e) {
		try {
			IMapWrapper pcrwcm = new PointCardRWConstantMapper();
			String msg = DeviceProperties.getProperties(service, pcrwcm);
			JTextArea jta = new JTextArea(msg);
			@SuppressWarnings("serial")
			JScrollPane jsp = new JScrollPane(jta) {
				@Override
				public Dimension getPreferredSize() {
					return new Dimension(460, 390);
				}
			};
			JOptionPane.showMessageDialog(null, jsp, "Information", JOptionPane.INFORMATION_MESSAGE);

		} catch (Exception jpe) {
			JOptionPane.showMessageDialog(null, "Exception in Info\nException: " + jpe.getMessage(),
					"Exception", JOptionPane.ERROR_MESSAGE);
			System.err.println("Jpos exception " + jpe);
		}
	}

	/**
	 * Shows statistics of device if they are supported by the device
	 */
	@Override
	@FXML
	public void handleStatistics(ActionEvent e) {
		String[] stats = new String[] { "", "U_", "M_" };
		try {
			((PointCardRW) service).retrieveStatistics(stats);
			DOMParser parser = new DOMParser();
			parser.parse(new InputSource(new java.io.StringReader(stats[1])));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(new ByteArrayInputStream(stats[1].getBytes()));

			printStatistics(doc.getDocumentElement(), "");

			JOptionPane.showMessageDialog(null, statistics, "Statistics", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (JposException jpe) {
			jpe.printStackTrace();
			JOptionPane.showMessageDialog(null, "Statistics are not supported!", "Statistics",
					JOptionPane.ERROR_MESSAGE);
		}

		statistics = "";
	}

	/*
	 * setUp ComboBoxes 
	 */
	
	private void setUpMapMode() {
		mapMode.getItems().clear();
		mapMode.getItems().add(PointCardRWConstantMapper.PCRW_MM_DOTS.getConstant());
		mapMode.getItems().add(PointCardRWConstantMapper.PCRW_MM_TWIPS.getConstant());
		mapMode.getItems().add(PointCardRWConstantMapper.PCRW_MM_ENGLISH.getConstant());
		mapMode.getItems().add(PointCardRWConstantMapper.PCRW_MM_METRIC.getConstant());
	}

	private void setUpRotationMode() {
		rotationMode.getItems().clear();
		rotationMode.getItems().add(PointCardRWConstantMapper.PCRW_RP_NORMAL.getConstant());
		rotationMode.getItems().add(PointCardRWConstantMapper.PCRW_RP_RIGHT90.getConstant());
		rotationMode.getItems().add(PointCardRWConstantMapper.PCRW_RP_LEFT90.getConstant());
		rotationMode.getItems().add(PointCardRWConstantMapper.PCRW_RP_ROTATE180.getConstant());
	}

	/**
	 * Sets the CharacterSetComboBox Values corresponding to the allowed Values
	 * for this device.
	 */
	private void setUpCharacterSet() {
		characterSet.getItems().clear();
		try {
			for (int i = 0; i < ((PointCardRW) service).getCharacterSetList().split(",").length; i++) {
				characterSet.getItems().add(
						Integer.parseInt((((PointCardRW) service).getCharacterSetList().split(","))[i]));
				if (i == 0) {
					characterSet.setValue(Integer.parseInt((((PointCardRW) service).getCharacterSetList()
							.split(","))[i]));
				}
			}

		} catch (JposException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error occured when getting the CharacterSetList",
					"Error occured!", JOptionPane.WARNING_MESSAGE);
		}
	}

}
