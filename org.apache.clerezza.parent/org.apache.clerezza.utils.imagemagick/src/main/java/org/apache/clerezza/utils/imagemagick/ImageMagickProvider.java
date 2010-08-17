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
package org.apache.clerezza.utils.imagemagick;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.utils.imageprocessing.ImageProcessor;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.ExifTagDataSet;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.IptcDataSet;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.MetaData;
import org.apache.clerezza.utils.imageprocessing.metadataprocessing.MetaDataProcessor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;

/**
 * This class implements interfaces that execute system calls to imageMagick.
 * 
 * <p>
 * Note: ImageMagick must be installed in the machine this service is running
 * on. ImageMagick is free open-source software to edit bitmap images on most
 * platforms. More information and binaries as well as source code can be found
 * at: <a href='http://www.imagemagick.org/'>http://www.imagemagick.org/</a>.
 * </p>
 * 
 * @author tio, hasan, daniel
 */
@Component(metatype=true)
@Properties({	
	@Property(name="convert", value="convert", description="Specifies the ImageMagick convert command."),
	@Property(name="identify", value="identify", description="Specifies the ImageMagick identify command."),
	@Property(name="release_number", intValue=6, description="Specifies ImageMagick release number (Syntax: release.version.majorRevision-minorRevision)."),
	@Property(name="version_number", intValue=5, description="Specifies ImageMagick version number (Syntax: release.version.majorRevision-minorRevision)."),
	@Property(name="major_release_number", intValue=2, description="Specifies ImageMagick major revision number (Syntax: release.version.majorRevision-minorRevision)."),
	@Property(name="minor_release_number", intValue=10, description="Specifies ImageMagick minor revision number (Syntax: release.version.majorRevision-minorRevision)."),
	@Property(name="service.ranking", value="100")
	})
@Services({
	@Service(ImageProcessor.class),
	@Service(MetaDataProcessor.class)
	})

public class ImageMagickProvider extends ImageProcessor implements MetaDataProcessor {

	private String convert = "convert";
	private String identify = "identify";
	private int imagemagickRelease = 6;
	private int imagemagickVersion = 5;
	private int imagemagickRevisionMajorNumber = 2;
	private int imagemagickRevisionMinorNumber = 10;

	@Reference
	private Serializer serializer;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected void activate(ComponentContext cCtx) {
		if (cCtx != null) {
			convert = (String) cCtx.getProperties().get("convert");
			identify = (String) cCtx.getProperties().get("identify");
			imagemagickRelease = (Integer) cCtx.getProperties().
				get("release_number");
			imagemagickVersion = (Integer) cCtx.getProperties().
				get("version_number");
			imagemagickRevisionMajorNumber = (Integer) cCtx.getProperties().
				get("major_release_number");
			imagemagickRevisionMinorNumber = (Integer) cCtx.getProperties().
				get("minor_release_number");
		}
		
		checkImageMagickInstallation();
		
		logger.info("ImageMagickProvider activated");
	}

	/**
	 * Default Constructor
	 */
	public ImageMagickProvider() {
		this.serializer = Serializer.getInstance();
	}
	
	/**
	 * This method checks if ImageMagick is correctly installed.
	 * You can configure the required version of ImageMagick
	 * via service properties.
	 * 
	 *  @throws RuntimeException    when no ImageMagick installation
	 *  							is found or the version is too
	 *  							low.
	 */
	protected void checkImageMagickInstallation() throws RuntimeException {
		boolean ok = true;
		
		try {
			List<String> command = new ArrayList<String>();
			command.add(identify);
			command.add("--version");
			Process proc = execCommand(command);
			
			BufferedReader br = new BufferedReader(
					new InputStreamReader(proc.getInputStream()));
			String output = br.readLine();
			br.close();
			
			ok = checkImageMagickVersion(output, imagemagickRelease, 
								imagemagickVersion, 
								imagemagickRevisionMajorNumber, 
								imagemagickRevisionMinorNumber);
			
			command.clear();
			command.add(convert);
			command.add("--version");
			proc = execCommand(command);
			
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			output = br.readLine();
			br.close();
			
			if(output!=null && !output.contains("Version: ImageMagick")) {
				ok = false;
			}
			
			
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			logger.warn("ImageMagick version check has been interrupted. " +
					"Assuming correct version.");
		} catch (IOException ex) {
			//this occurs when the commands are miising
			ok = false;
		} catch (NullPointerException ex) {
			//can occur when output is empty (e.g. imagemagick prints
			//only error messages which go to stderror)
			ok = false;
		}
		
		if(!ok) {
			logger.error("ImageMagick version can not be verified. " +
			"Please make sure you have ImageMagick (>=" +
			imagemagickRelease + "." + imagemagickVersion + "." +
			imagemagickRevisionMajorNumber + "-" + 
			imagemagickRevisionMinorNumber +
			") installed correctly");
			
			throw new RuntimeException("ImageMagick not installed correctly.");
		}
	}

	private boolean checkImageMagickVersion(String str, int release, int version,
			int revision_major_number, int revision_minor_number) {

		Pattern pattern = Pattern.compile("(\\d+\\.){2}\\d+-\\d+");
		Matcher matcher = pattern.matcher(str);

		boolean error = false;
		if (matcher.find()) {
			String versionString = matcher.group();
			String[] versionParts = versionString.split("\\.");
			if (Integer.parseInt(versionParts[0]) < release) {
				error = true;
			} else if (Integer.parseInt(versionParts[0]) == release) {
				if (Integer.parseInt(versionParts[1]) < version) {
					error = true;
				} else if (Integer.parseInt(versionParts[1]) == version) {
					String[] revisionParts = versionParts[2].split("-");
					if (Integer.parseInt(revisionParts[0]) < revision_major_number) {
						error = true;
					} else if (Integer.parseInt(revisionParts[0]) == revision_major_number) {
						if (Integer.parseInt(revisionParts[1]) < revision_minor_number) {
							error = true;
						}
					}
				}
			}
			return !error;
		} else {
			return false;
		}
	}

	@Override
	public BufferedImage makeImageTranslucent(BufferedImage image,
			float translucency) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public BufferedImage makeColorTransparent(BufferedImage image, Color color) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public BufferedImage flip(BufferedImage image, int direction) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		if (direction == 0) {
			command.add("-flop");
		} else {
			command.add("-flip");
		}

		return processImage(command, 100, image);
	}

	@Override
	public BufferedImage rotate(BufferedImage image, int angle) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		command.add("-rotate");
		command.add("" + angle);

		return processImage(command, 100, image);
	}

	@Override
	public BufferedImage resize(BufferedImage image, int newWidth, int newHeight) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		command.add("-geometry");
		command.add(newWidth + "x" + newHeight + "!");

		return processImage(command, 100, image);
	}

	@Override
	public BufferedImage resizeProportional(BufferedImage image, int newWidth,
			int newHeight) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		command.add("-geometry");
		if (newWidth != 0) {
			command.add("" + newWidth);
		} else {
			if (newHeight != 0) {
				command.add("x" + newHeight);
			} else {
				return image;
			}
		}

		return processImage(command, 100, image);
	}

	@Override
	public BufferedImage resizeRelative(BufferedImage image,
			float resizeFactorWidth, float resizeFactorHeight) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		command.add("-geometry");
		command.add((100 * resizeFactorWidth) + "%x"
				+ (100 * resizeFactorHeight) + "%");

		return processImage(command, 100, image);
	}

	@Override
	public BufferedImage resizeRelativeProportional(BufferedImage image,
			float resizeFactor) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		command.add("-geometry");
		command.add(100 * resizeFactor + "%");

		return processImage(command, 100, image);
	}

	private BufferedImage crop(BufferedImage image, int newWidth, int newHeight) {
		List<String> command = new ArrayList<String>(10);
		command.add(convert);
		command.add("-crop");
		command.add(newWidth + "x" + newHeight);

		return processImage(command, 100, image);
	}

	@Override
	public MetaData<IptcDataSet> extractIPTC(byte[] mediaFile) {
		List<String> command = new ArrayList<String>(3);
		command.add(convert);
		command.add("-");
		command.add("IPTCTEXT:-");
		
		try {
			List<String> resultLines = inputStreamToStringList(execCommand(
					command, mediaFile).getInputStream());
			MetaData<IptcDataSet> metaData = new MetaData<IptcDataSet>();
			
			for (String line : resultLines) {
				// ImageMagick specific output processing
				// output has the form of:
				// recordNumber#dataSetNumber#recordName="value"
				// recordNumber is always 2 as imagemagick only reads the record 2
				// values.
				try {
					int pos;
					int dataSetNumber;
					boolean hasPropertyName = true;
					if ((pos = line.indexOf('#', 2)) > -1 && pos < 6) {
						//output contains a property name (normal situation)
						dataSetNumber = Integer.parseInt(line.substring(2, pos));
					} else {
						// output doesn't contains a property name (e.g. record
						// version: 2#0="value")
						pos = line.indexOf('=');
						hasPropertyName = false;
						dataSetNumber = Integer.parseInt(line.substring(2, pos));
					}
	
					if (hasPropertyName) {
						//jump to the value part if data set contains recordName.
						pos = line.indexOf('=');
					}
					
					metaData.add(new IptcDataSet(2, dataSetNumber, 
							line.substring(pos + 2, line.length() - 1)));
	
				} catch (NumberFormatException ex) {
					logger.info(
							"Could not parse IPTC record number for DataSet: {}",
							line);
					// format of the line is corrupt. nothing can be done about it,
					// we try the next line
					continue;
				}
			}
			
			return metaData;
		} catch (IOException ex) {
			logger.warn("IOException while trying to execute {}", command);
		} catch (InterruptedException ex) {
			logger.warn("ImageMagick has been interrupted");
			Thread.currentThread().interrupt();
		}
		return null;
	}

	@Override
	public MetaData<ExifTagDataSet> extractEXIF(byte[] mediaFile) {
		List<String> command = new ArrayList<String>(4);
		command.add(identify);
		command.add("-format");
		command.add("%[exif:*]");
		command.add("-");

		try {
			List<String> resultLines = inputStreamToStringList(execCommand(
					command, mediaFile).getInputStream());
			MetaData<ExifTagDataSet> metaData = new MetaData<ExifTagDataSet>();
			
			for (String line : resultLines) {
				// ImageMagick specific output processing
				// output has the form of: exif:tagName=value
	
				line = line.trim();
				if (line.length() > 5) {
					// line not empty (contains more than "exif:")
					// we don't need the "exif:" part
					String[] sa = line.substring(5).split("=");
					// sa[0] contains the tagName, sa[1] contains the value
					try {
						try {
							// special handling of some wrongly named exif tags
							if (sa[0].equals("ExifImageLength")) {
								sa[0] = "ImageLength";
							} else if (sa[0].equals("ExifImageWidth")) {
								sa[0] = "ImageWidth";
							}
	
							metaData.add(new ExifTagDataSet(sa[0], sa[1]));
						} catch (ArrayIndexOutOfBoundsException ex) {
							// the data set has no value or contains no equal (=)
							// sign.
							if (sa.length == 1) {
								// assume empty value
								metaData.add(new ExifTagDataSet(sa[0], ""));
							} else {
								logger.info("Could not identify EXIF tag in: {}",
										line);
								continue;
							}
						}
					} catch (NoSuchFieldException ex) {
						logger.info("Could not identify EXIF tagName in: {}", line);
						continue;
					}
				}
			}
			
			return metaData;
			
		} catch (IOException ex) {
			logger.warn("IOException while trying to execute {}", command);
		} catch (InterruptedException ex) {
			logger.warn("ImageMagick has been interrupted");
			Thread.currentThread().interrupt();
		}
		return null;
	}

	@Override
	public TripleCollection extractXMP(byte[] mediaFile) {
		List<String> command = new ArrayList<String>(3);
		command.add(convert);
		command.add("-");
		command.add("XMP:-");
		
		
		try {
			Iterator<String> it = inputStreamToStringList(
					execCommand(command, mediaFile).getInputStream()).iterator();
			
			StringBuilder sb = new StringBuilder();
			while (it.hasNext()) {
				//ImageMagick specific output processing
				
				String line = it.next().trim();
				if(line.equals("")) {
					continue;
				}
				if(line.startsWith("<?") || line.startsWith("<x:") ||
						line.startsWith("</x:")) {
					continue;
				}

				sb.append(" " + line);
			}
			
			ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString()
					.getBytes());
			return Parser.getInstance().parse(bais, "application/rdf+xml");
			
		} catch (IOException ex) {
			logger.warn("IOException while trying to execute {}", command);
		} catch (InterruptedException ex) {
			logger.warn("ImageMagick has been interrupted");
			Thread.currentThread().interrupt();
		}
		return null;
	}

	@Override
	public byte[] writeXMP(byte[] mediaFile,
			TripleCollection metaData) {
		
		synchronized(this) {
			//I use files because i couldn't find a way to supply 
			//3 streams as arguments to imagemagick
			//using files requires this block to be synchronized
			//possibly this can be solved by using one of the java APIs
			//for imagemagick
			File profile = new File("tmpFile.rdf");
			File inFile = new File("tmpFile2.jpg");
			File outFile = new File("tmpFile3.jpg");

			List<String> command = new ArrayList<String>(5);
			command.add(convert);
			command.add("-profile");
			command.add("XMP:" + profile.getName());
			command.add(inFile.getName());
			command.add(outFile.getName());

			try {
				FileOutputStream fos = new FileOutputStream(profile);

				//write XMP header
				fos.write("<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n".getBytes());
				fos.write("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n".getBytes());

				serializer.serialize(fos, metaData,"application/rdf+xml");

				fos.write("\n</x:xmpmeta>".getBytes());
				fos.write("\n<?xpacket end=\"w\"?>".getBytes());

				fos.close();

				FileOutputStream fos2 = new FileOutputStream(inFile);
				fos2.write(mediaFile);
				fos2.close();

				execCommand(command);

				FileInputStream fis = new FileInputStream(outFile);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int ch;
				while((ch = fis.read()) != -1) {
					baos.write(ch);
				}
				fis.close();

				return baos.toByteArray();

			} catch (IOException ex) {
				logger.warn("IOException while trying to execute {}", command);
			} catch (InterruptedException ex) {
				logger.warn("ImageMagick has been interrupted");
				Thread.currentThread().interrupt();
			} finally {
				profile.delete();
				inFile.delete();
				outFile.delete();
			}
		}
		
		return null;
	}

	
	
	private BufferedImage processImage(List<String> command, int quality,
			BufferedImage image) {
		command.add("-quality");
		command.add(String.valueOf(quality));
		command.add("-");
		command.add("-");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (ImageIO.write(image, "png", baos) == false) {
				logger.warn("Cannot write image to output stream");
				return null;
			}

			return ImageIO.read(execCommand(command, baos.toByteArray()).
					getInputStream());
			
		} catch (InterruptedException ex) {
			logger.warn("ImageMagick has been interrupted");
			Thread.currentThread().interrupt();
			return null;
		} catch (IOException ex) {
			logger.warn("IOException while trying to execute {}", command);
			return null;
		}
	}

	private Process execCommand(List<String> command,
			byte[]... inputData) throws IOException,
			InterruptedException {
		logger.info("Trying to execute command {}", command);
		Process proc = new ProcessBuilder(command).start();
		for(byte[] bytes : inputData) {
			proc.getOutputStream().write(bytes);
		}
		proc.getOutputStream().close();
		
		if (proc.waitFor() > 0) {
			//an error occurred
			StringBuilder sb = new StringBuilder();
			Iterator<String> it;
			try {
				it = inputStreamToStringList(
						proc.getErrorStream()).iterator();
				
				while (it.hasNext()) {
					sb.append(it.next());
					sb.append("\n");
				}
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			} finally {
				logger.warn("Error in ImageMagick while trying to execute {}. Error: {} ",
								command, sb.toString());
			}
		}
		return proc;
	}

	private List<String> inputStreamToStringList(InputStream is)
			throws UnsupportedEncodingException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				"utf-8"));

		List<String> sl = new ArrayList<String>();
		String line;
		while ((line = br.readLine()) != null) {
			sl.add(line);
		}
		br.close();

		return sl;
	}
}
