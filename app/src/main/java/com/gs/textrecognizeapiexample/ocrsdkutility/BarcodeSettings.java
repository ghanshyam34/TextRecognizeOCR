
package com.gs.textrecognizeapiexample.ocrsdkutility;


public class BarcodeSettings {

	public String asUrlParams() {
		return "barcodeType=" + barcodeType;
	}

	public String getType() {
		return barcodeType;
	}

	public void setType(String newType) {
		barcodeType = newType;
	}

	private String barcodeType = "autodetect";
}
