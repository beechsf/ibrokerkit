package ibrokerkit.ibrokerfront.webapplication.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;

public class GoogleGeoCoder {

	protected static final String GOOGLE_MAPS_API_URL = "http://maps.google.com/maps/geo?";
	protected static final String GOOGLE_MAPS_API_KEY = "ABQIAAAAml-MGQsfmsFeYqkiZXPV6RSeNIu6onlBE4TBQcLjAJmW4xHajRQH6myTAPZp7BmMDJQ7JQYDzXtkow";

	public static GoogleGeoCoderResult geoCode(String address) throws IOException {

		String query;

		try {

			query = new URLCodec().encode(address);
		} catch (EncoderException ex) {

			throw new IOException(ex.getMessage());
		}

		StringBuffer urlString = new StringBuffer(GOOGLE_MAPS_API_URL);
		urlString.append("q=" + query);
		urlString.append("&key=" + GOOGLE_MAPS_API_KEY);
		urlString.append("&output=csv");

		URL url = new URL(urlString.toString());

		String resultString = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
		String[] resultParts = resultString.split(",");

		if (! (resultParts.length == 4)) throw new IOException();
		if (! (resultParts[0].trim().equals("200"))) throw new IOException();

		return(new GoogleGeoCoderResult(resultParts));
	}

	public static class GoogleGeoCoderResult {

		private Double lat;
		private Double lng;
		private Double zoom;

		public GoogleGeoCoderResult(String[] parts) {
			this.lat = new Double(parts[2]);
			this.lng = new Double(parts[3]);
			this.zoom = new Double(parts[1]);
		}

		public GoogleGeoCoderResult(Double lat, Double lng, Double zoom) {
			this.lat = lat;
			this.lng = lng;
			this.zoom = zoom;
		}

		public Double getLat() {
			return (this.lat);
		}
		public void setLat(Double lat) {
			this.lat = lat;
		}
		public Double getLng() {
			return (this.lng);
		}
		public void setLng(Double lng) {
			this.lng = lng;
		}
		public Double getZoom() {
			return (this.zoom);
		}
		public void setZoom(Double zoom) {
			this.zoom = zoom;
		}
	}
}
