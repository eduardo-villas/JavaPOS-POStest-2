/*
 * Copyright 2013 NTS New Technology Systems GmbH. All Rights reserved.
 * NTS PROPRIETARY/CONFIDENTIAL. Use is subject to NTS License Agreement.
 * Address: Doernbacher Strasse 126, A-4073 Wilhering, Austria
 * Homepage: www.ntswincash.com
 */
package postest2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jpos.JposException;
import jpos.LineDisplay;
import jpos.profile.JposDevCats;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LineDisplayController extends CommonController implements Initializable {

	@FXML
	@RequiredState(JposState.ENABLED)
	public TabPane functionTab;

	// Display Text
	@FXML
	public ComboBox<Integer> row;
	@FXML
	public ComboBox<Integer> column;
	@FXML
	public ComboBox<String> attribute;
	@FXML
	public TextField displayText;

	@FXML
	public TextField blinkRate;
	@FXML
	public TextField intercharacterWait;

	// Misc
	@FXML
	public ComboBox<Integer> descriptors;
	@FXML
	public ComboBox<String> descriptor_attribute;
	@FXML
	public ComboBox<String> scrollText_direction;
	@FXML
	public TextField scrollText_Units;
	@FXML
	public TextField readCharacterField;
	@FXML
	public TextField glypeCode;
	@FXML
	public TextField glyphBinaryPath;
	@FXML
	public ComboBox<Integer> characterSet;
	@FXML
	public Slider deviceBrightness;

	// Window
	@FXML
	public TextField viewportRow;
	@FXML
	public TextField viewportColumn;
	@FXML
	public TextField viewportHeight;
	@FXML
	public TextField viewportWidth;
	@FXML
	public TextField windowHeight;
	@FXML
	public TextField windowWidth;
	@FXML
	public ListView<String> openWindowsListView;

	// Display Marquee
	@FXML
	public TextField marqueeRepeatWait;
	@FXML
	public TextField marqueeUnitWait;
	@FXML
	public ComboBox<String> marqueeType;
	@FXML
	public ComboBox<String> marqueeFormat;

	// Display Bitmap
	@FXML
	public ComboBox<String> alignmentX;
	@FXML
	public ComboBox<String> alignmentY;
	@FXML
	public ComboBox<String> bitmapWidth;
	@FXML
	public TextField bitmapPath;

	// Screen Mode
	@FXML
	@RequiredState(JposState.CLAIMED)
	public TabPane setScreenModeTab;
	@FXML
	public ComboBox<String> screenMode;

	// OpenWindow-Counter
	private int currentWindow = 0;

	// List for ListView openWindowsListView
	private ObservableList<String> windowList = FXCollections.observableArrayList();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		setUpLogicalNameComboBox();
		setUpAttribute();
		service = new LineDisplay();
		RequiredStateChecker.invokeThis(this, service);
	}

	private void setUpLogicalNameComboBox() {
		logicalName.setItems(LogicalNameGetter.getLogicalNamesByCategory(JposDevCats.LINEDISPLAY_DEVCAT
				.toString()));
	}

	/* ************************************************************************
	 * ************************ Action Handler ********************************
	 * ************************************************************************
	 */

	/**
	 * Need this to set the ComboBox for Screen mode. (only available if device
	 * is claimed but not enabled)
	 */
	@FXML
	@Override
	public void handleClaim(ActionEvent e) {
		setUpScreenMode();
		deviceEnabled.setSelected(true);
		super.handleClaim(e);
	}

	@FXML
	public void handleDeviceEnable(ActionEvent e) {
		try {
			if (deviceEnabled.isSelected()) {
				((LineDisplay) service).setDeviceEnabled(true);
				setUpRow();
				setUpColumns();
				setUpDescriptors();
				setUpDescriptorAttribute();
				setUpScrollTextDirection();
				setUpCharacterSet();
				setUpMarqueeType();
				setUpMarqueeFormat();
				setUpBitmapWidth();
				setUpAlignmentX();
				setUpAlignmentY();

				windowList.clear();

				windowList.add("0");
				openWindowsListView.setItems(windowList);

			} else {
				((LineDisplay) service).setDeviceEnabled(false);
			}
			RequiredStateChecker.invokeThis(this, service);
		} catch (JposException je) {
			JOptionPane.showMessageDialog(null, je.getMessage());
		}
	}

	@Override
	@FXML
	public void handleOCE(ActionEvent e) {
		super.handleOCE(e);
		handleDeviceEnable(e);
	}

	@FXML
	public void handleDisplayTextAt(ActionEvent e) {
		if (row.getSelectionModel().getSelectedItem() == null) {
			JOptionPane.showMessageDialog(null, "Row is not selected!", "Logical name is empty",
					JOptionPane.WARNING_MESSAGE);
		}
		try {
			((LineDisplay) service).displayTextAt(row.getSelectionModel().getSelectedIndex(), column
					.getSelectionModel().getSelectedItem(), displayText.getText(), attribute
					.getSelectionModel().getSelectedIndex());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleDisplayText(ActionEvent e) {
		try {
			((LineDisplay) service).displayText(displayText.getText(), attribute.getSelectionModel()
					.getSelectedIndex());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleClearText(ActionEvent e) {
		try {
			((LineDisplay) service).clearText();
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleMoveCursor(ActionEvent e) {
		try {
			((LineDisplay) service).setCursorColumn(column.getSelectionModel().getSelectedIndex());
			((LineDisplay) service).setCursorRow(row.getSelectionModel().getSelectedIndex());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleSetBlinkRate(ActionEvent e) {
		if (blinkRate.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Param blinkRate is not set!", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				((LineDisplay) service).setBlinkRate(Integer.parseInt(blinkRate.getText()));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	@FXML
	public void handleSetICharWait(ActionEvent e) {
		if (intercharacterWait.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Param ICharWait is not set!", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				((LineDisplay) service).setInterCharacterWait(Integer.parseInt(intercharacterWait.getText()));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	@FXML
	public void handleAddWindow(ActionEvent e) {
		if (viewportRow.getText().equals("") || viewportColumn.getText().equals("")
				|| viewportHeight.getText().equals("") || viewportWidth.getText().equals("")
				|| windowHeight.getText().equals("") || windowWidth.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "One of the params is not set!", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				FXCollections.sort(windowList);
				int num = 0;
				for (String s : windowList) {
					if (s.equals("" + num)) {
						num++;
					}
					if (num == 4) {
						JOptionPane.showMessageDialog(null, "Too many open Windows!", "Invalid Parameter",
								JOptionPane.WARNING_MESSAGE);
						return;
					}
				}

				windowList.add("" + num);

				FXCollections.sort(windowList);
				((LineDisplay) service).createWindow(Integer.parseInt(viewportRow.getText()),
						Integer.parseInt(viewportColumn.getText()),
						Integer.parseInt(viewportHeight.getText()),
						Integer.parseInt(viewportWidth.getText()), Integer.parseInt(windowHeight.getText()),
						Integer.parseInt(windowWidth.getText()));

			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}

		}
	}

	@FXML
	public void handleDeleteWindow(ActionEvent e) {
		try {
			((LineDisplay) service).destroyWindow();
			windowList.remove("" + currentWindow);
			FXCollections.sort(windowList);
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
	}

	@FXML
	public void handleRefreshWindow(ActionEvent e) {
		if (openWindowsListView.getSelectionModel().getSelectedItem() == null) {
			JOptionPane.showMessageDialog(null, "Choose a valid window!", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				((LineDisplay) service).refreshWindow(Integer.parseInt(openWindowsListView
						.getSelectionModel().getSelectedItem()));
				currentWindow = Integer.parseInt(openWindowsListView.getSelectionModel().getSelectedItem());
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	@FXML
	public void handleScrollText(ActionEvent e) {
		if (scrollText_direction.getSelectionModel().getSelectedItem().equals("")) {
			JOptionPane.showMessageDialog(null, "Choose a valid scroll direction!", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else if (scrollText_Units.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Choose a valid unit!", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				((LineDisplay) service).scrollText(scrollText_direction.getSelectionModel()
						.getSelectedIndex(), Integer.parseInt(scrollText_Units.getText()));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	@FXML
	public void handleSetDeviceBrightness(ActionEvent e) {
		try {
			((LineDisplay) service).setDeviceBrightness((int) (deviceBrightness.getValue()));
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleSetDescriptor(ActionEvent e) {
		try {
			((LineDisplay) service).setDescriptor(descriptors.getSelectionModel().getSelectedIndex(),
					descriptor_attribute.getSelectionModel().getSelectedIndex());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
	}

	@FXML
	public void handleClearAllDescriptors(ActionEvent e) {
		try {
			((LineDisplay) service).clearDescriptors();
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
	}

	@FXML
	public void handleReadCharacter(ActionEvent e) {
		int[] help = new int[1];
		try {
			((LineDisplay) service).readCharacterAtCursor(help);
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
		readCharacterField.setText("" + help[0]);

	}

	@FXML
	public void handleDefineGlyph(ActionEvent e) throws JposException {
		if (glyphBinaryPath.getText() == "") {
			JOptionPane.showMessageDialog(null, "Choose a valid glyph path", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else if (glypeCode.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Choose a valid Glyph Code", "Invalid Parameter",
					JOptionPane.WARNING_MESSAGE);
		} else {
			byte[] bytes = getBytesFromFile(glyphBinaryPath.getText());
			((LineDisplay) service).defineGlyph(Integer.parseInt(glypeCode.getText()), bytes);
		}
	}

	@FXML
	public void handleSelectGlyphBinary(ActionEvent e) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose Glyph Binary");
		File f = chooser.showOpenDialog(null);
		if (f != null) {
			glyphBinaryPath.setText(f.getAbsolutePath());
		}
	}

	@FXML
	public void handleSetCharacterSet(ActionEvent e) {
		try {
			((LineDisplay) service).setCharacterSet(characterSet.getSelectionModel().getSelectedItem());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleSetScreenMode(ActionEvent e) {
		try {
			((LineDisplay) service).setScreenMode(screenMode.getSelectionModel().getSelectedIndex());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
	}

	@FXML
	public void handleSetMarqueeType(ActionEvent e) {
		try {
			((LineDisplay) service).setMarqueeType(marqueeType.getSelectionModel().getSelectedIndex());
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

	}

	@FXML
	public void handleSetMarqueeFormat(ActionEvent e) {
		try {
			((LineDisplay) service).setMarqueeFormat(marqueeFormat.getSelectionModel().getSelectedIndex());

		} catch (NumberFormatException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
	}

	@FXML
	public void handleSetMarqueeRepeatWait(ActionEvent e) {
		if (marqueeRepeatWait.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Param marqueeRepeatWait is false", "Invalid Parameter!",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				((LineDisplay) service).setMarqueeRepeatWait(Integer.parseInt(marqueeRepeatWait.getText()));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	@FXML
	public void handleSetMarqueeUnitWait(ActionEvent e) {
		if (marqueeUnitWait.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Param marqueeUnitWait is false", "Invalid Parameter!",
					JOptionPane.WARNING_MESSAGE);
		} else {
			try {
				((LineDisplay) service).setMarqueeUnitWait(Integer.parseInt(marqueeUnitWait.getText()));
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	@FXML
	public void handleChooseBitmap(ActionEvent e) {

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose Bitmap Path");
		File f = chooser.showOpenDialog(null);
		if (f != null) {
			bitmapPath.setText(f.getAbsolutePath());
		}
	}

	@FXML
	public void handleDisplayBitmap(ActionEvent e) {
		if (bitmapPath.getText().equals("")) {
			JOptionPane.showMessageDialog(null, "Param bitmapPath is not set");
		} else {
			// Calculate Bitmap Width
			int newBitmapWidth = 0;
			if (bitmapWidth.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_ASIS.getConstant())) {
				newBitmapWidth = LineDisplayConstantMapper.DISP_BM_ASIS.getContantNumber();
			} else {
				newBitmapWidth = Integer.parseInt(bitmapWidth.getSelectionModel().getSelectedItem());
			}

			// Calculate AlignmentX
			int newAlignmentX = 0;
			if (alignmentX.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_LEFT.getConstant())) {
				newAlignmentX = LineDisplayConstantMapper.DISP_BM_LEFT.getContantNumber();

			} else if (alignmentX.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_CENTER.getConstant())) {

				newAlignmentX = LineDisplayConstantMapper.DISP_BM_CENTER.getContantNumber();
			} else if (alignmentX.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_RIGHT.getConstant())) {

				newAlignmentX = LineDisplayConstantMapper.DISP_BM_RIGHT.getContantNumber();
			} else {
				newAlignmentX = Integer.parseInt(alignmentX.getSelectionModel().getSelectedItem());
			}

			// Calculate AlignmentX
			int newAlignmentY = 0;
			if (alignmentY.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_BOTTOM.getConstant())) {

				newAlignmentY = LineDisplayConstantMapper.DISP_BM_BOTTOM.getContantNumber();

			} else if (alignmentY.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_CENTER.getConstant())) {

				newAlignmentY = LineDisplayConstantMapper.DISP_BM_CENTER.getContantNumber();

			} else if (alignmentY.getSelectionModel().getSelectedItem()
					.equals(LineDisplayConstantMapper.DISP_BM_TOP.getConstant())) {

				newAlignmentY = LineDisplayConstantMapper.DISP_BM_TOP.getContantNumber();

			} else {

				newAlignmentY = Integer.parseInt(alignmentY.getSelectionModel().getSelectedItem());
			}

			try {
				((LineDisplay) service).displayBitmap(bitmapPath.getText(), newBitmapWidth, newAlignmentX,
						newAlignmentY);
			} catch (NumberFormatException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			} catch (JposException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage());
			}
		}
	}

	/**
	 * This Method gets a Byte Array from a File to print it with
	 * displayMemoryBitmap
	 * 
	 * @param path
	 *            to Binary File
	 * @return byte[] containing the data from the File
	 */
	private byte[] getBytesFromFile(String path) {
		byte[] bytes = null;
		BufferedImage originalImage = null;
		try {
			originalImage = ImageIO.read(new File(path));
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}
		if (originalImage == null) {
			JOptionPane.showMessageDialog(null, "Image has a Bad Format!");
			return null;
		}

		// change Imgage Format
		BufferedImage newBufferedImage = new BufferedImage(originalImage.getWidth(),
				originalImage.getHeight(), BufferedImage.TYPE_BYTE_BINARY);

		newBufferedImage.createGraphics().drawImage(originalImage, 0, 0, Color.WHITE, null);

		// convert BufferedImage to byte array
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(newBufferedImage, "bmp", baos);
			baos.flush();

			bytes = baos.toByteArray();
			baos.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, e1.getMessage());
		}

		return bytes;
	}

	/* ************************************************************************
	 * ************************ Set all ComboBox Values ***********************
	 * ************************************************************************
	 */

	private void setUpAttribute() {
		attribute.getItems().clear();
		attribute.getItems().add(LineDisplayConstantMapper.DISP_DT_NORMAL.getContantNumber(),
				LineDisplayConstantMapper.DISP_DT_NORMAL.getConstant());
		attribute.getItems().add(LineDisplayConstantMapper.DISP_DT_BLINK.getContantNumber(),
				LineDisplayConstantMapper.DISP_DT_BLINK.getConstant());
		attribute.getItems().add(LineDisplayConstantMapper.DISP_DT_REVERSE.getContantNumber(),
				LineDisplayConstantMapper.DISP_DT_REVERSE.getConstant());
		attribute.getItems().add(LineDisplayConstantMapper.DISP_DT_BLINK_REVERSE.getContantNumber(),
				LineDisplayConstantMapper.DISP_DT_BLINK_REVERSE.getConstant());
		attribute.setValue(LineDisplayConstantMapper.DISP_DT_NORMAL.getConstant());

	}

	private void setUpRow() {
		row.getItems().clear();
		try {
			for (int i = 0; i < ((LineDisplay) service).getRows(); i++) {
				row.getItems().add(i);
			}
		} catch (JposException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error occured when getting Rows", "Error occured!",
					JOptionPane.WARNING_MESSAGE);
		}
		row.setValue(0);
	}

	private void setUpColumns() {
		column.getItems().clear();
		try {
			for (int i = 0; i < ((LineDisplay) service).getColumns(); i++) {
				column.getItems().add(i);
			}
		} catch (JposException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error occured when getting Columns", "Error occured!",
					JOptionPane.WARNING_MESSAGE);
		}
		column.setValue(0);
	}

	private void setUpDescriptors() {
		descriptors.getItems().clear();
		try {
			descriptors.getItems().add(((LineDisplay) service).getDeviceDescriptors());
			descriptors.setValue(((LineDisplay) service).getDeviceDescriptors());
		} catch (JposException e1) {
			e1.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error occured when getting Descriptors", "Error occured!",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private void setUpDescriptorAttribute() {
		descriptor_attribute.getItems().clear();
		descriptor_attribute.getItems().add(LineDisplayConstantMapper.DISP_SD_OFF.getContantNumber(),
				LineDisplayConstantMapper.DISP_SD_OFF.getConstant());
		descriptor_attribute.getItems().add(LineDisplayConstantMapper.DISP_SD_ON.getContantNumber(),
				LineDisplayConstantMapper.DISP_SD_ON.getConstant());
		descriptor_attribute.getItems().add(LineDisplayConstantMapper.DISP_SD_BLINK.getContantNumber(),
				LineDisplayConstantMapper.DISP_SD_BLINK.getConstant());
		descriptor_attribute.setValue(LineDisplayConstantMapper.DISP_SD_OFF.getConstant());
	}

	private void setUpScrollTextDirection() {
		scrollText_direction.getItems().clear();

		// Need for correct Index
		scrollText_direction.getItems().add(0, "");
		scrollText_direction.getItems().add(LineDisplayConstantMapper.DISP_ST_UP.getContantNumber(),
				LineDisplayConstantMapper.DISP_ST_UP.getConstant());
		scrollText_direction.getItems().add(LineDisplayConstantMapper.DISP_ST_DOWN.getContantNumber(),
				LineDisplayConstantMapper.DISP_ST_DOWN.getConstant());
		scrollText_direction.getItems().add(LineDisplayConstantMapper.DISP_ST_LEFT.getContantNumber(),
				LineDisplayConstantMapper.DISP_ST_LEFT.getConstant());
		scrollText_direction.getItems().add(LineDisplayConstantMapper.DISP_ST_RIGHT.getContantNumber(),
				LineDisplayConstantMapper.DISP_ST_RIGHT.getConstant());

		scrollText_direction.setValue(LineDisplayConstantMapper.DISP_ST_UP.getConstant());

	}

	private void setUpCharacterSet() {
		characterSet.getItems().clear();
		try {
			for (int i = 0; i < ((LineDisplay) service).getCharacterSetList().split(",").length; i++) {
				characterSet.getItems().add(
						Integer.parseInt((((LineDisplay) service).getCharacterSetList().split(","))[i]));
				if (i == 0) {
					characterSet.setValue(Integer.parseInt((((LineDisplay) service).getCharacterSetList()
							.split(","))[i]));
				}
			}

		} catch (JposException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error occured when getting the CharacterSetList",
					"Error occured!", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void setUpScreenMode() {
		screenMode.getItems().clear();
		try {
			for (int i = 0; i < ((LineDisplay) service).getScreenModeList().split(",").length; i++) {
				screenMode.getItems().add((((LineDisplay) service).getScreenModeList().split(","))[i]);
				if (i == 0) {
					screenMode.setValue((((LineDisplay) service).getScreenModeList().split(","))[i]);
				}
			}
		} catch (JposException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error occured when getting the ScreenModeList",
					"Error occured!", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void setUpMarqueeType() {
		marqueeType.getItems().clear();
		marqueeType.getItems().add(LineDisplayConstantMapper.DISP_MT_NONE.getContantNumber(),
				LineDisplayConstantMapper.DISP_MT_NONE.getConstant());
		marqueeType.getItems().add(LineDisplayConstantMapper.DISP_MT_UP.getContantNumber(),
				LineDisplayConstantMapper.DISP_MT_UP.getConstant());
		marqueeType.getItems().add(LineDisplayConstantMapper.DISP_MT_DOWN.getContantNumber(),
				LineDisplayConstantMapper.DISP_MT_DOWN.getConstant());
		marqueeType.getItems().add(LineDisplayConstantMapper.DISP_MT_LEFT.getContantNumber(),
				LineDisplayConstantMapper.DISP_MT_LEFT.getConstant());
		marqueeType.getItems().add(LineDisplayConstantMapper.DISP_MT_RIGHT.getContantNumber(),
				LineDisplayConstantMapper.DISP_MT_RIGHT.getConstant());
		marqueeType.getItems().add(LineDisplayConstantMapper.DISP_MT_INIT.getContantNumber(),
				LineDisplayConstantMapper.DISP_MT_INIT.getConstant());

		marqueeType.setValue(LineDisplayConstantMapper.DISP_MT_NONE.getConstant());
	}

	private void setUpMarqueeFormat() {
		marqueeFormat.getItems().clear();
		marqueeFormat.getItems().add(LineDisplayConstantMapper.DISP_MF_WALK.getContantNumber(),
				LineDisplayConstantMapper.DISP_MF_WALK.getConstant());
		marqueeFormat.getItems().add(LineDisplayConstantMapper.DISP_MF_PLACE.getContantNumber(),
				LineDisplayConstantMapper.DISP_MF_PLACE.getConstant());

		marqueeFormat.setValue(LineDisplayConstantMapper.DISP_MF_WALK.getConstant());
	}

	private void setUpBitmapWidth() {
		bitmapWidth.getItems().clear();
		bitmapWidth.getItems().add(LineDisplayConstantMapper.DISP_BM_ASIS.getConstant());

		bitmapWidth.setValue(LineDisplayConstantMapper.DISP_BM_ASIS.getConstant());
	}

	private void setUpAlignmentX() {
		alignmentX.getItems().clear();
		alignmentX.getItems().add(LineDisplayConstantMapper.DISP_BM_LEFT.getConstant());
		alignmentX.getItems().add(LineDisplayConstantMapper.DISP_BM_CENTER.getConstant());
		alignmentX.getItems().add(LineDisplayConstantMapper.DISP_BM_RIGHT.getConstant());

		alignmentX.setValue(LineDisplayConstantMapper.DISP_BM_RIGHT.getConstant());
	}

	private void setUpAlignmentY() {
		alignmentY.getItems().clear();
		alignmentY.getItems().add(LineDisplayConstantMapper.DISP_BM_TOP.getConstant());
		alignmentY.getItems().add(LineDisplayConstantMapper.DISP_BM_CENTER.getConstant());
		alignmentY.getItems().add(LineDisplayConstantMapper.DISP_BM_BOTTOM.getConstant());

		alignmentY.setValue(LineDisplayConstantMapper.DISP_BM_BOTTOM.getConstant());
	}

	// Shows statistics of device if they are supported by the device
	@Override
	@FXML
	public void handleInfo(ActionEvent e) {
		try {
			String msg = DeviceProperties.getProperties(service, null);
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

	// Shows statistics of device if they are supported by the device
	@Override
	@FXML
	public void handleStatistics(ActionEvent e) {
		String[] stats = new String[] { "", "U_", "M_" };
		try {
			((LineDisplay) service).retrieveStatistics(stats);
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
}
