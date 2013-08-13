/*
 * Copyright 2013 NTS New Technology Systems GmbH. All Rights reserved.
 * NTS PROPRIETARY/CONFIDENTIAL. Use is subject to NTS License Agreement.
 * Address: Doernbacher Strasse 126, A-4073 Wilhering, Austria
 * Homepage: www.ntswincash.com
 */
package postest2;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;

public class BillDispenserController extends CommonController implements Initializable {

	@FXML
	private ComboBox<String> logicalName;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
	
	/* ************************************************************************
	 * ************************ Action Handler *********************************
	 * ***********************************************************************
	 */

	/*
	 * Action Handler
	 */
	@FXML
	public void handle(ActionEvent e) {
	}
}