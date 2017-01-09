package com.gs.textrecognizeapiexample.ocrsdkutility;

public class TextFieldSettings {
	public String asUrlParams() {
		// For all possible parameters, see documentation at
		// http://ocrsdk.com/documentation/apireference/processTextField/
		return String.format("language=%s&textType=%s", language, textType);
	}

	public void setLanguage(String newLanguage) {
		language = newLanguage;
	}

	public String getLanguage() {
		return language;
	}

	public String getTextType() {
		return textType;
	}

	public void setTextType(String newTextType) {
		textType = newTextType;
	}

	private String language = "English";
	private String textType = "normal,handprinted";
}
