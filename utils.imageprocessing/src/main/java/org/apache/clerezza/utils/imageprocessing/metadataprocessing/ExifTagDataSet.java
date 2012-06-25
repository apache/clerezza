/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.utils.imageprocessing.metadataprocessing;

import java.lang.reflect.Field;

/**
 * This class represents an EXIF Tag and defines 
 * EXIF specific functionality and data.
 * 
 * @author daniel
 *
 */
public class ExifTagDataSet extends DataSet {
	
	/**
	 * @see DataSet#FORMAT_NAME
	 */
	static final String EXIF_FORMAT_NAME = "EXIF";

	private final String tagName;
	private final int tagNumber;
	private String value;
	
	/**
	 * Constructor.
	 * 
	 * @param tagName  the tag name
	 * @param value  the value
	 * @throws NoSuchFieldException  
	 * 			If the supplied tagName can't be resolved to a EXIF Tag.
	 */
	public ExifTagDataSet(String tagName, String value) throws NoSuchFieldException {
		this.tagName = tagName;
		this.value = value;
		this.tagNumber = extractTagNumber(tagName);
	}

	/**
	 * Constructor.
	 * 
	 * @param tagName  the tag name
	 * @param value  the value
	 */
	public ExifTagDataSet(int tagNumber, String value) {
		this.tagNumber = tagNumber;
		this.value = value;
		this.tagName = extractTagName(tagNumber);
	}
	
	/**
	 * Returns the EXIF tag name.
	 * 
	 * @return the tagName
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * Returns the EXIF tag number.
	 * 
	 * @return the tagNumber
	 */
	public int getTagNumber() {
		return tagNumber;
	}

	@Override
	public String getKey() {
		return String.valueOf(tagNumber);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void set(String value) {
		this.value = value;
	}
	
	private int extractTagNumber(String tagName) throws NoSuchFieldException {
		int tagNumber = -1;
		for(Field field : getClass().getDeclaredFields()) {
			if(field.getName().equalsIgnoreCase(tagName)) {
				try {
					tagNumber = field.getInt(null);
					break;
				} catch (RuntimeException ex) {
					logger.info("Can't create EXIF data set with tag name \"{}\"", tagName);
					throw ex;
				} catch(IllegalAccessException ex) {
					logger.info("Can't create EXIF data set with tag name \"{}\"", tagName);
					throw new RuntimeException(ex);
				}
			}
		}
		if(tagNumber == -1) {
			throw new NoSuchFieldException("There is no field with name: " + tagName);
		}
		return tagNumber;
	}
	
	private String extractTagName(int value) {
		String name = "";
		for(Field field : getClass().getDeclaredFields()) {
			if(field.getType().getName().equals(int.class.getName())) {
				try {
					if(field.getInt(null) == value) {
						name = field.getName();
					}
				} catch (Exception ex) {
					//IllegalAccess or IllegalFormatException 
					continue;
				}
			}
		}
		
		return name;
	}

	@Override
	public boolean equals(Object other) {
		if(other != null && this.getClass().equals(other.getClass())) {
			ExifTagDataSet o = (ExifTagDataSet) other;
			return o.tagName.equals(tagName) && o.value.equals(value);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (tagName + value).hashCode();
	}
	
	/**
	 * EXIF ExifVersion key
	 */
	public static final int ExifVersion = 36864;
	
	/**
	 * EXIF FlashPixVersion key
	 */
	public static final int FlashPixVersion = 40960;
	
	/**
	 * EXIF ColorSpace key
	 */
	public static final int ColorSpace = 40961;
	
	/**
	 * EXIF ComponentsConfiguration key
	 */
	public static final int ComponentsConfiguration = 37121;

	/**
	 * EXIF CompressedBitsPerPixel key
	 */
	public static final int CompressedBitsPerPixel = 37122;

	/**
	 * EXIF PixelXDimension key
	 */
	public static final int PixelXDimension = 40962;
	
	/**
	 * EXIF PixelYDimension key
	 */
	public static final int PixelYDimension = 40963;
	
	/**
	 * EXIF MakerNote key
	 */
	public static final int MakerNote = 37500;

	/**
	 * EXIF UserComment key
	 */
	public static final int UserComment = 37510;

	/**
	 * EXIF RelatedSoundFile key
	 */
	public static final int RelatedSoundFile = 40964;

	/**
	 * EXIF DateTimeOriginal key
	 */
	public static final int DateTimeOriginal = 36867;

	/**
	 * EXIF DateTimeDigitized key
	 */
	public static final int DateTimeDigitized = 36868;

	/**
	 * EXIF SubSecTime key
	 */
	public static final int SubSecTime = 37520;

	/**
	 * EXIF SubSecTimeOriginal key
	 */
	public static final int SubSecTimeOriginal = 37521;

	/**
	 * EXIF SubSecTimeDigitized key
	 */
	public static final int SubSecTimeDigitized = 37522;

	/**
	 * EXIF ImageUniqueID key
	 */
	public static final int ImageUniqueID = 42016;

	/**
	 * EXIF ExposureTime key
	 */
	public static final int ExposureTime = 33434;

	/**
	 * EXIF FNumber key
	 */
	public static final int FNumber = 33437;

	/**
	 * EXIF ExposureProgram key
	 */
	public static final int ExposureProgram = 34850;

	/**
	 * EXIF SpectralSensitivity key
	 */
	public static final int SpectralSensitivity = 34852;

	/**
	 * EXIF ISOSpeedRatings key
	 */
	public static final int ISOSpeedRatings = 34855;

	/**
	 * EXIF OECF key
	 */
	public static final int OECF = 34856;

	/**
	 * EXIF ShutterSpeedValue key
	 */
	public static final int ShutterSpeedValue = 37377;

	/**
	 * EXIF ApertureValue key
	 */
	public static final int ApertureValue = 37378;

	/**
	 * EXIF BrightnessValue key
	 */
	public static final int BrightnessValue = 37379;

	/**
	 * EXIF ExposureBiasValue key
	 */
	public static final int ExposureBiasValue = 37380;

	/**
	 * EXIF MaxApertureValue key
	 */
	public static final int MaxApertureValue = 37381;

	/**
	 * EXIF SubjectDistance key
	 */
	public static final int SubjectDistance = 37382;

	/**
	 * EXIF MeteringMode key
	 */
	public static final int MeteringMode = 37383;

	/**
	 * EXIF LightSource key
	 */
	public static final int LightSource = 37384;

	/**
	 * EXIF Flash key
	 */
	public static final int Flash = 37385;

	/**
	 * EXIF FocalLength key
	 */
	public static final int FocalLength = 37386;

	/**
	 * EXIF SubjectArea key
	 */
	public static final int SubjectArea = 37396;

	/**
	 * EXIF FlashEnergy key
	 */
	public static final int FlashEnergy = 41483;

	/**
	 * EXIF SpatialFrequencyResponse key
	 */
	public static final int SpatialFrequencyResponse = 41484;

	/**
	 * EXIF FocalPlaneXResolution key
	 */
	public static final int FocalPlaneXResolution = 41486;

	/**
	 * EXIF FocalPlaneYResolution key
	 */
	public static final int FocalPlaneYResolution = 41487;

	/**
	 * EXIF FocalPlaneResolutionUnit key
	 */
	public static final int FocalPlaneResolutionUnit = 41488;

	/**
	 * EXIF SubjectLocation key
	 */
	public static final int SubjectLocation = 41492;

	/**
	 * EXIF ExposureIndex key
	 */
	public static final int ExposureIndex = 41493;

	/**
	 * EXIF SensingMethod key
	 */
	public static final int SensingMethod = 41495;

	/**
	 * EXIF FileSource key
	 */
	public static final int FileSource = 41728;

	/**
	 * EXIF SceneType key
	 */
	public static final int SceneType = 41729;

	/**
	 * EXIF CFAPattern key
	 */
	public static final int CFAPattern = 41730;

	/**
	 * EXIF CustomRendered key
	 */
	public static final int CustomRendered = 41985;

	/**
	 * EXIF ExposureMode key
	 */
	public static final int ExposureMode = 41986;

	/**
	 * EXIF WhiteBalance key
	 */
	public static final int WhiteBalance = 41987;

	/**
	 * EXIF DigitalZoomRatio key
	 */
	public static final int DigitalZoomRatio = 41988;

	/**
	 * EXIF FocalLengthIn35mmFilm key
	 */
	public static final int FocalLengthIn35mmFilm = 41989;

	/**
	 * EXIF SceneCaptureType key
	 */
	public static final int SceneCaptureType = 41990;

	/**
	 * EXIF GainControl key
	 */
	public static final int GainControl = 41991;

	/**
	 * EXIF Contrast key
	 */
	public static final int Contrast = 41992;

	/**
	 * EXIF Saturation key
	 */
	public static final int Saturation = 41993;

	/**
	 * EXIF Sharpness key
	 */
	public static final int Sharpness = 41994;

	/**
	 * EXIF DeviceSettingDescription key
	 */
	public static final int DeviceSettingDescription = 41995;

	/**
	 * EXIF SubjectDistanceRange key
	 */
	public static final int SubjectDistanceRange = 41996;

	//TIFF Tags
	
	/**
	 * TIFF ImageWidth key
	 */
	public static final int ImageWidth = 256;

	/**
	 * TIFF ImageLength key
	 */
	public static final int ImageLength = 257;

	/**
	 * TIFF BitsPerSample key
	 */
	public static final int BitsPerSample = 258;
	
	/**
	 * TIFF Compression key
	 */
	public static final int Compression = 259;

	/**
	 * TIFF PhotometricInterpretation key
	 */
	public static final int PhotometricInterpretation = 262;

	/**
	 * TIFF ImageDescription key
	 */
	public static final int ImageDescription = 270;

	/**
	 * TIFF Make key
	 */
	public static final int Make = 271;

	/**
	 * TIFF Model key
	 */
	public static final int Model = 272;

	/**
	 * TIFF StripOffsets key
	 */
	public static final int StripOffsets = 273;

	/**
	 * TIFF Orientation key
	 */
	public static final int Orientation = 274;
	
	/**
	 * TIFF SamplesPerPixel key
	 */
	public static final int SamplesPerPixel = 277;
	
	/**
	 * TIFF RowsPerStrip key
	 */
	public static final int RowsPerStrip = 278;
	
	/**
	 * TIFF StripByteCounts key
	 */
	public static final int StripByteCounts = 279;
	
	/**
	 * TIFF XResolution key
	 */
	public static final int XResolution = 282;

	/**
	 * TIFF YResolution key
	 */
	public static final int YResolution = 283;
	
	/**
	 * TIFF PlanarConfiguration key
	 */
	public static final int PlanarConfiguration = 284;
	
	/**
	 * TIFF ResolutionUnit key
	 */
	public static final int ResolutionUnit = 296;

	/**
	 * TIFF TransferFunction key
	 */
	public static final int TransferFunction = 301;
	
	/**
	 * TIFF Software key
	 */
	public static final int Software = 305;
	
	/**
	 * TIFF DateTime key
	 */
	public static final int DateTime = 306;

	/**
	 * TIFF Artist key
	 */
	public static final int Artist = 315;

	/**
	 * TIFF WhitePoint key
	 */
	public static final int WhitePoint = 318;

	/**
	 * TIFF PrimaryChromaticities key
	 */
	public static final int PrimaryChromaticities = 319;

	/**
	 * TIFF JPEGInterchangeFormat key
	 */
	public static final int JPEGInterchangeFormat = 513;

	/**
	 * TIFF JPEGInterchangeFormatLength key
	 */
	public static final int JPEGInterchangeFormatLength = 514;
	
	/**
	 * TIFF YCbCrCoefficients key
	 */
	public static final int YCbCrCoefficients = 529;
	
	/**
	 * TIFF YCbCrSubSampling key
	 */
	public static final int YCbCrSubSampling = 530;
	
	/**
	 * TIFF YCbCrPositioning key
	 */
	public static final int YCbCrPositioning = 531;
	
	/**
	 * TIFF ReferenceBlackWhite key
	 */
	public static final int ReferenceBlackWhite = 532;
	
	/**
	 * TIFF Copyright key
	 */
	public static final int Copyright = 33432;
	
	//GPS Tags
	
	/**
	 * GPS GPSVersionID key
	 */
	public static final int GPSVersionID = 0;

	/**
	 * GPS GPSLatitudeRef key
	 */
	public static final int GPSLatitudeRef = 1;

	/**
	 * GPS GPSLatitude key
	 */
	public static final int GPSLatitude = 2;
	
	/**
	 * GPS GPSLongitudeRef key
	 */
	public static final int GPSLongitudeRef = 3;

	/**
	 * GPS GPSLongitude key
	 */
	public static final int GPSLongitude = 4;
	
	/**
	 * GPS GPSAltitudeRef key
	 */
	public static final int GPSAltitudeRef = 5;
	
	/**
	 * GPS GPSAltitude key
	 */
	public static final int GPSAltitude = 6;
	
	/**
	 * GPS GPSTimeStamp key
	 */
	public static final int GPSTimeStamp = 7;
	
	/**
	 * GPS GPSSatellites key
	 */
	public static final int GPSSatellites = 8;
	
	/**
	 * GPS GPSStatus key
	 */
	public static final int GPSStatus = 9;
	
	/**
	 * GPS GPSMeasureMode key
	 */
	public static final int GPSMeasureMode = 10;
	
	/**
	 * GPS GPSDOP key
	 */
	public static final int GPSDOP = 11;
	
	/**
	 * GPS GPSSpeedRef key
	 */
	public static final int GPSSpeedRef = 12;
	
	/**
	 * GPS GPSSpeed key
	 */
	public static final int GPSSpeed = 13;
	
	/**
	 * GPS GPSTrackRef key
	 */
	public static final int GPSTrackRef = 14;
	
	/**
	 * GPS GPSTrack key
	 */
	public static final int GPSTrack = 15;
	
	/**
	 * GPS GPSImgDirectionRef key
	 */
	public static final int GPSImgDirectionRef = 16;
	
	/**
	 * GPS GPSImgDirection key
	 */
	public static final int GPSImgDirection = 17;
	
	/**
	 * GPS GPSMapDatum key
	 */
	public static final int GPSMapDatum = 18;
	
	/**
	 * GPS GPSDestLatitudeRef key
	 */
	public static final int GPSDestLatitudeRef = 19;
	
	/**
	 * GPS GPSDestLatitude key
	 */
	public static final int GPSDestLatitude = 20;
	
	/**
	 * GPS GPSDestLongitudeRef key
	 */
	public static final int GPSDestLongitudeRef = 21;
	
	/**
	 * GPS GPSDestLongitude key
	 */
	public static final int GPSDestLongitude = 22;
	
	/**
	 * GPS GPSDestBearingRef key
	 */
	public static final int GPSDestBearingRef = 23;
	
	/**
	 * GPS GPSDestBearing key
	 */
	public static final int GPSDestBearing = 24;
	
	/**
	 * GPS GPSDestDistanceRef key
	 */
	public static final int GPSDestDistanceRef = 25;

	/**
	 * GPS GPSDestDistance key
	 */
	public static final int GPSDestDistance = 26;
	
	static {
		//mapping for conversion of EXIF tags to XMP tags.
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ExifVersion), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifVersion);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FlashPixVersion), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFlashpixVersion);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ColorSpace), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifColorSpace);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ComponentsConfiguration), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifComponentsConfiguration);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(CompressedBitsPerPixel), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifCompressedBitsPerPixel);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(PixelXDimension), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifPixelXDimension);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(PixelYDimension), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifPixelYDimension);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(UserComment), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifUserComment);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(RelatedSoundFile), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifRelatedSoundFile);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(DateTimeOriginal), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifDateTimeOriginal);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(DateTimeDigitized), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifDateTimeDigitized);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ImageUniqueID), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifImageUniqueID);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ExposureTime), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifExposureTime);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FNumber), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFNumber);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ExposureProgram), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifExposureProgram);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SpectralSensitivity), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSpectralSensitivity);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ISOSpeedRatings), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifISOSpeedRatings);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(OECF), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifOECF);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ShutterSpeedValue), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifShutterSpeedValue);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ApertureValue), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifApertureValue);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(BrightnessValue), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifBrightnessValue);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ExposureBiasValue), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifExposureBiasValue);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(MaxApertureValue), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifMaxApertureValue);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SubjectDistance), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSubjectDistance);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(MeteringMode), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifMeteringMode);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(LightSource), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifLightSource);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Flash), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFlash);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FocalLength), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFocalLength);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SubjectArea), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSubjectArea);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FlashEnergy), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFlashEnergy);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SpatialFrequencyResponse), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSpatialFrequencyResponse);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FocalPlaneXResolution), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFocalPlaneXResolution);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FocalPlaneYResolution), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFocalPlaneYResolution);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FocalPlaneResolutionUnit), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFocalPlaneResolutionUnit);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SubjectLocation), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSubjectLocation);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ExposureIndex), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifExposureIndex);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SensingMethod), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSensingMethod);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FileSource), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFileSource);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SceneType), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSceneType);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(CFAPattern), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifCFAPattern);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(CustomRendered), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifCustomRendered);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ExposureMode), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifExposureMode);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(WhiteBalance), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifWhiteBalance);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(DigitalZoomRatio), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifDigitalZoomRatio);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(FocalLengthIn35mmFilm), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifFocalLengthIn35mmFilm);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SceneCaptureType), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSceneCaptureType);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GainControl), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGainControl);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Contrast), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifContrast);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Saturation), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSaturation);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Sharpness), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSharpness);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(DeviceSettingDescription), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifDeviceSettingDescription);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(SubjectDistanceRange), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifSubjectDistanceRange);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ImageWidth), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffImageWidth);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ImageLength), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffImageLength);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(BitsPerSample), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffBitsPerSample);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Compression), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffCompression);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(PhotometricInterpretation), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffPhotometricInterpretation);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ImageDescription), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffImageDescription);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Make), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffMake);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Model), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffModel);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(XResolution), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffXResolution);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(YResolution), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffYResolution);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(PlanarConfiguration), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffPlanarConfiguration);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ResolutionUnit), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffResolutionUnit);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(TransferFunction), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffTransferFunction);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Software), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffSoftware);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(DateTime), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffDateTime);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Artist), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffArtist);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(WhitePoint), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffWhitePoint);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(PrimaryChromaticities), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffPrimaryChromaticities);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(YCbCrCoefficients), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffYCbCrCoefficients);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(YCbCrSubSampling), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffYCbCrSubSampling);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(YCbCrPositioning), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffYCbCrPositioning);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(ReferenceBlackWhite), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffReferenceBlackWhite);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(Copyright), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.TiffCopyright);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSVersionID), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSVersionID);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSLatitude), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSLatitude);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSLongitude), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSLongitude);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSAltitudeRef), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSAltitudeRef);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSAltitude), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSAltitude);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSTimeStamp), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSTimeStamp);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSSatellites), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSSatellites);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSStatus), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSStatus);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSMeasureMode), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSMeasureMode);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDOP), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDOP);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSSpeedRef), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSSpeedRef);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSSpeed), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSSpeed);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSTrackRef), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSTrackRef);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSTrack), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSTrack);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSImgDirectionRef), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSImgDirectionRef);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSImgDirection), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSImgDirection);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSMapDatum), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSMapDatum);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDestLatitude), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDestLatitude);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDestLongitude), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDestLongitude);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDestBearingRef), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDestBearingRef);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDestBearing), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDestBearing);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDestDistanceRef), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDestDistanceRef);
		conversionMap.put(new DataSetFormatPair(
				String.valueOf(GPSDestDistance), EXIF_FORMAT_NAME),
				XmpSchemaDefinitions.ExifGPSDestDistance);
	}
}
