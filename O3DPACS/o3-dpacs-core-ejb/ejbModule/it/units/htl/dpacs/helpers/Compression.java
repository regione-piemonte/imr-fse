 /*
 * SPDX-FileCopyrightText: (C) Copyright 2022 Regione Piemonte
 *
 * SPDX-License-Identifier: GPL-2.0
 */

package it.units.htl.dpacs.helpers;

import com.sun.media.imageio.plugins.jpeg2000.J2KImageWriteParam;
import com.sun.media.imageio.stream.SegmentedImageInputStream;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Hashtable;
import javax.imageio.IIOImage;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmDecodeParam;
import org.dcm4che.data.DcmEncodeParam;
import org.dcm4che.data.DcmParser;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.DcmParserFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.data.FileMetaInfo;
import org.dcm4che.dict.Tags;
import org.dcm4che.dict.UIDs;
import org.dcm4che.dict.VRs;
import org.dcm4che.util.BufferedOutputStream;
import org.dcm4cheri.image.ImageReaderFactory;
import org.dcm4cheri.image.ImageWriterFactory;
import org.dcm4cheri.image.ItemParser;

/**
 * This class performs compression and decompression.
 * The class has a companion Mbean for setting runtime parameters,
 * essentially the temp dir for the moment
 *
 * Only lossless .90 and .70 (Jpeg p14 and Jpeg2000) transfer syntaxes are
 * supported for the moment.
 * This means you can compress and decompress only to and from those formats.
 *
 * REMEBER you need JAI image IO tools for JDK on the JVM used to start the 
 * Application Server
 * 
 * @author Carrara, sango
 */
public class Compression {

    static final String YBR_RCT = "YBR_RCT";
    static final String JPEG2000 = "jpeg2000";
    static final String JPEG = "jpeg";
    static final String JPEG_LOSSLESS = "JPEG-LOSSLESS";
    static final String JPEG_LS = "JPEG-LS";
    static final Log log = LogFactory.getLog(Compression.class);
    private static final byte[] ITEM_TAG = {(byte) 0xfe, (byte) 0xff, (byte) 0x00, (byte) 0xe0};
    private static final int[] GRAY_BAND_OFFSETS = {0};
    private static final int[] RGB_BAND_OFFSETS = {0, 1, 2};
    protected int dataType;

    /** Creates a new instance of Compression */
    public Compression() {
    }

    /** Compress an uncompressed Dicom Image, into a compressed one.
     *  The compression is available with TransferSyntax JPEGLossless
     *  and  JPEG2000Lossless.
     *
     * @param  sourceFile               Name of the source file
     * @param  tsuid                    Transfer Syntax UID of the destination file
     * @param  tempUrl                  Path of the destination file
     *
     * @exception  IOException            We read and write on files
     * @exception  FileNotFoundException  If one file is not found
     *
     * @return The File object that identifies the compressed image.
     * If the Transfer Syntax is not JPEGLossless or JPEG2000Lossless,
     * the File object is null.
     *
     */
    public File compressImage(File sourceFile, String tsuid, String tempUrl, int pixelDataVR) throws FileNotFoundException, IOException {
    	FileInputStream fis = new FileInputStream(sourceFile);
    	return compressImage(fis, tsuid, tempUrl, pixelDataVR);
    }
    
    public File compressImage(InputStream source, String tsuid, String tempUrl, int pixelDataVR) throws FileNotFoundException, IOException {
    	File destFile = null;
    	if ((!tsuid.equalsIgnoreCase(UIDs.JPEGLossless)) && (!tsuid.equalsIgnoreCase(UIDs.JPEG2000Lossless))) {
    		log.warn("Compression is not available with Transfer Syntax: " + tsuid);
    	} else {
    		log.debug("Compressing with Transfer Syntax: " + tsuid);
    		BufferedInputStream bis = new BufferedInputStream(source);
    		try {
    			DcmParser parser = DcmParserFactory.getInstance().newDcmParser(bis);
    			Dataset ds = DcmObjectFactory.getInstance().newDataset();
    			parser.setDcmHandler(ds.getDcmHandler());
    			parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
    			FileMetaInfo fmi = DcmObjectFactory.getInstance().newFileMetaInfo(ds, tsuid);
    			ds.setFileMetaInfo(fmi);
    			destFile = writeCompressedDataset(ds, parser, tsuid, tempUrl, bis, pixelDataVR);
    		} finally {
    			try{
    				bis.close();
    			}catch(Exception ex){}
    			try{
    				source.close();
    			}catch(Exception ex){}
    		}
    	}
    	return destFile;
    }

    /** Compress an uncompressed Dicom Image, into a compressed one.
     *  The compression is available with TransferSyntax JPEGLossless
     *  and  JPEG2000Lossless.
     *
     * @param  inputDataset               Name of the source dataset
     * @param  tsuid                    Transfer Syntax UID of the destination file
     * @param  tempUrl                  Path of the destination file
     *
     * @exception  IOException            We read and write on files
     * @exception  FileNotFoundException  If one file is not found
     *
     * @return The File object that identifies the compressed image.
     * If the Transfer Syntax is not JPEGLossless or JPEG2000Lossless,
     * the File object is null.
     *
     */
    public File compressDataset(Dataset inputDataset, String tsuid, String tempUrl) throws FileNotFoundException, IOException {
        log.debug("dataset bytes= " + inputDataset.length());
        File destFile = null;
        if ((!tsuid.equalsIgnoreCase(UIDs.JPEGLossless)) && (!tsuid.equalsIgnoreCase(UIDs.JPEG2000Lossless))) {
            log.fatal("Compression is not available with Transfer Syntax: " + tsuid);
            return null;
        } else {
            log.debug("Compressing with Transfer Syntax: " + tsuid);
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new ByteArrayInputStream(inputDataset.getByteBuffer(Tags.PixelData).array()));
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(bis);
            parser.setDcmHandler(inputDataset.getDcmHandler());
            destFile = writeCompressedDataset(inputDataset.subSet(0, Tags.PixelData), parser, tsuid, tempUrl, bis, VRs.OB);
            bis.close();
        } catch (Exception e) {
            log.error("", e);
        }
        return destFile;
    }

    public Compression getInstance() {
        return this;
    }

    /**
     * Writes the dataset to a file with the selected transfer syntax UID
     * @param ds the dataset to write 
     * @param parser the DICOM parser you used to parse the dataset
     * @param tsuid the trasfer syntax to use to write the file
     * @param tempUrl the path to the temporary directory for compressing file
     * @param bis the stream associated to the parser (should be useful to obtain pixeldata)
     * @return 
     */
    private File writeCompressedDataset(Dataset ds, DcmParser parser, String tsuid, String tempUrl, BufferedInputStream bis, int pixelDataVR) {
        File destFile = new File(tempUrl);
        try {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }
            byte[] buffer = new byte[8192];
            FileOutputStream fos = new FileOutputStream(destFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer);
            try {
                DcmDecodeParam decParam = parser.getDcmDecodeParam();
                DcmEncodeParam encParam = DcmEncodeParam.valueOf(tsuid);
                ds.writeFile(bos, encParam);
                ds.writeHeader(bos, encParam, Tags.PixelData, pixelDataVR, -1);
                int samples = ds.getInt(Tags.SamplesPerPixel, 1);
                int frames = ds.getInt(Tags.NumberOfFrames, 1);
                int rows = ds.getInt(Tags.Rows, 1);
                int columns = ds.getInt(Tags.Columns, 1);
                int bitsAllocated = ds.getInt(Tags.BitsAllocated, 8);
                int bitsStored = ds.getInt(Tags.BitsStored, bitsAllocated);
                int bitsUsed = isOverlayInPixelData(ds) ? bitsAllocated : bitsStored;
                int pixelRepresentation = ds.getInt(Tags.PixelRepresentation, 0);
                int planarConfiguration = ds.getInt(Tags.PlanarConfiguration, 0);
                int frameLength = rows * columns * samples * bitsAllocated / 8;
                int pixelDataLength = frameLength * frames;
                ImageWriter writer = null;
                long end = 0;
                try {
                    boolean supportSigned = true; //???
                    ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
                    ios.setByteOrder(ByteOrder.LITTLE_ENDIAN);
                    writer = ImageWriterFactory.getInstance().getWriterForTransferSyntax(tsuid);
                    ImageWriteParam wParam = writer.getDefaultWriteParam();
                    supportSigned = initWriteParam(wParam, tsuid, ds, samples);
                    switch (bitsAllocated) {
                        case 8:
                            dataType = DataBuffer.TYPE_BYTE;
                            break;
                        case 16:
                            dataType = pixelRepresentation == 0 || !supportSigned ? DataBuffer.TYPE_USHORT : DataBuffer.TYPE_SHORT;
                            break;
                        default:
                            throw new IllegalArgumentException("bits allocated:" + bitsAllocated);
                    }
                    WritableRaster raster = Raster.createWritableRaster(getSampleModel(planarConfiguration, columns, dataType, rows, samples), null);
                    DataBuffer db = raster.getDataBuffer();
                    BufferedImage bi = new BufferedImage(getColorModel(samples, bitsUsed), raster, false, null);
                    ios.write(ITEM_TAG);
                    ios.writeInt(0);
                    for (int i = 0; i < frames; ++i) {
                        ios.write(ITEM_TAG);
                        long mark = ios.getStreamPosition();
                        ios.writeInt(0);
                        switch (dataType) {
                            case DataBuffer.TYPE_BYTE:
                                read(bis, ((DataBufferByte) db).getBankData());
                                break;
                            case DataBuffer.TYPE_SHORT:
                                read(decParam.byteOrder, bis, ((DataBufferShort) db).getBankData());
                                break;
                            case DataBuffer.TYPE_USHORT:
                                read(decParam.byteOrder, bis, ((DataBufferUShort) db).getBankData());
                                break;
                            default:
                                throw new RuntimeException("dataType:" + db.getDataType());
                        }
                        writer.setOutput(ios);
                        writer.write(null, new IIOImage(bi, null, null), wParam);
                        end = ios.getStreamPosition();
                        if ((end & 1) != 0) {
                            ios.write(0);
                            ++end;
                        }
                        ios.seek(mark);
                        ios.writeInt((int) (end - mark - 4));
                        ios.seek(end);
                        ios.flush();
                    }
                } finally {
                    if (writer != null) {
                        writer.dispose();
                    }
                }
                pixelDataLength = frameLength * frames;
                ds.writeHeader(bos, encParam, Tags.SeqDelimitationItem, VRs.NONE, 0);
                bis.skip(parser.getReadLength() - pixelDataLength);
                parser.parseDataset(decParam, -1);
                ds.subSet(Tags.PixelData, -1).writeDataset(bos, encParam);
            } finally {
                bos.close();
            }
        } catch (Exception e) {
            log.error("", e);
            return null;
        }
        return destFile;
    }

    /** Decompress a compressed Dicom Image, into an uncompressed one,
     * with Transfer Syntax ExplicitVRLittleEndian or ImplicitVRLittleEndian.
     *
     * @param  sourceFile               Name of the source file
     * @param  tsuid                    Transfer Syntax UID of the destination file
     * @param  tempUrl                  Path of the destination file
     *
     * @exception  IOException            We read and write on files
     * @exception  FileNotFoundException  If one file is not found
     *
     * @return The File object that identifies the uncompressed image.
     * If the Transfer Syntax is not ExplicitVRLittleEndian or ImplicitVRLittleEndian,
     * the File object is null.
     *
     */
    public File decompressImage(File fileSource, String tsuid, String tempUrl) throws FileNotFoundException, IOException {
        Dataset ds = DcmObjectFactory.getInstance().newDataset();
        Dataset ready = DcmObjectFactory.getInstance().newDataset();
        FileImageInputStream fiis = new FileImageInputStream(fileSource);
        File temp = new File(tempUrl);
        byte[] buffer = new byte[8192];
        FileOutputStream fos = new FileOutputStream(temp);
        BufferedOutputStream bos = new BufferedOutputStream(fos, buffer);
        try {
            DcmParser parser = DcmParserFactory.getInstance().newDcmParser(fiis);
            parser.setDcmHandler(ds.getDcmHandler());
            parser.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            String initialTransfer = getTransferSyntax(ds);
            try {
                DcmEncodeParam encodeParam = DcmEncodeParam.valueOf(tsuid);
                // Parametri img
                int samples = ds.getInt(Tags.SamplesPerPixel, 1);
                int frames = ds.getInt(Tags.NumberOfFrames, 1);
                int rows = ds.getInt(Tags.Rows, 1);
                int columns = ds.getInt(Tags.Columns, 1);
                int bitsAllocated = ds.getInt(Tags.BitsAllocated, 8);
//                int bitsStored = ds.getInt(Tags.BitsStored, bitsAllocated);
                int frameLength = rows * columns * samples * bitsAllocated / 8;
                int pixelDataLength = frameLength * frames;
                // Parametri img
                FileMetaInfo fmi = DcmObjectFactory.getInstance().newFileMetaInfo(ds, tsuid);
                ds.setFileMetaInfo(fmi);
                int pxdataVR = parser.getReadVR();
                ds.writeFile(bos, encodeParam);
                ds.writeHeader(bos, encodeParam, Tags.PixelData, pxdataVR, (pixelDataLength + 1) & ~1);
                ImageReaderFactory imgReadFact = ImageReaderFactory.getInstance();
                
                ImageReader imgRead = imgReadFact.getReaderForTransferSyntax(initialTransfer);
                try {
                    ImageInputStream iis = parser.getImageInputStream();
                    ItemParser itemParser = new ItemParser(parser);
                    SegmentedImageInputStream siis = new SegmentedImageInputStream(iis, itemParser);
                    BufferedImage bufferedImg = createBufferedImage(samples, bitsAllocated, columns, rows);
                    for (int i = 0; i < frames; ++i) {
                        imgRead.setInput(siis); // settato al "puntatore" dello stream
                        ImageReadParam imgReadParam = imgRead.getDefaultReadParam();
                        imgReadParam.setDestination(bufferedImg); // dove salvare i byte letti
                        bufferedImg = imgRead.read(0, imgReadParam); // lettura a partire dall'inizio del reader
                        imgRead.reset(); // reset del reader
                        itemParser.seekNextFrame(siis); // sposto il "puntatore" nello stream
                        write(bufferedImg.getRaster(), bos, encodeParam.byteOrder); //writta!!!
                    }
                    itemParser.seekFooter();
//                    int a = pixelDataLength & 1;
                    if ((pixelDataLength & 1) != 0) {
                        bos.write(0);
                    }
                    parser.parseDataset(parser.getDcmDecodeParam(), Tags.PixelData);
                    ds.subSet(Tags.PixelData, -1).writeDataset(bos, encodeParam);
                    log.debug("final lenght " + bos.toString().length());
                }catch (Exception e) {
                    log.warn("Unable to decompress the file!", e);
		} finally {
                    if (imgRead != null) {
                        imgRead.dispose(); //release...
                    }
                }
            } catch (IOException ioe) {
                log.error("Unable to decompress the file!", ioe);
            }
        } finally {
            try {
                fiis.close();
                bos.close();
                fos.close();
            } catch (IOException ioe) {
                log.error("", ioe);
            }
            FileImageInputStream fiisReady = new FileImageInputStream(temp);
            DcmParser parserReady = DcmParserFactory.getInstance().newDcmParser(fiisReady);
            parserReady.setDcmHandler(ready.getDcmHandler());
            parserReady.parseDcmFile(FileFormat.DICOM_FILE, Tags.PixelData);
            try {
                fiisReady.close();
            } catch (IOException ex) {
                log.error("", ex);
            }
        }
        return temp;
    }

    private void write(WritableRaster raster, OutputStream output, ByteOrder byteOrder) throws IOException {
        DataBuffer buffer = raster.getDataBuffer();
        final int stride = ((ComponentSampleModel) raster.getSampleModel()).getScanlineStride();
        final int h = raster.getHeight();
        final int w = raster.getWidth();
        final int b = raster.getNumBands();
        final int wb = w * b;
        switch (buffer.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                for (int i = 0; i < h; ++i) {
                    output.write(((DataBufferByte) buffer).getData(), i * stride, wb);
                }
                break;
            case DataBuffer.TYPE_USHORT:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    log.debug("little endian start");
                    for (int i = 0; i < h; ++i) {
                        writeShortLE(((DataBufferUShort) buffer).getData(), i * stride, wb, output);
                    }
                } else {
                    for (int i = 0; i < h; ++i) {
                        writeShortBE(((DataBufferUShort) buffer).getData(), i * stride, wb, output);
                    }
                }
                break;
            case DataBuffer.TYPE_SHORT:
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
                    for (int i = 0; i < h; ++i) {
                        writeShortLE(((DataBufferShort) buffer).getData(), i * stride, wb, output);
                    }
                } else {
                    for (int i = 0; i < h; ++i) {
                        writeShortBE(((DataBufferShort) buffer).getData(), i * stride, wb, output);
                    }
                }
                break;
            default:
                throw new RuntimeException(buffer.getClass().getName() + " not supported");
        }
    }

    private void writeShortLE(short[] data, int off, int len, OutputStream out) throws IOException {
        for (int i = off, end = off + len; i < end; i++) {
            final short px = data[i];
            out.write(px & 0xff);
            out.write((px >>> 8) & 0xff);
        }
    }

    private void writeShortBE(short[] data, int off, int len, OutputStream out) throws IOException {
        for (int i = off, end = off + len; i < end; i++) {
            final short px = data[i];
            out.write((px >>> 8) & 0xff);
            out.write(px & 0xff);
        }
    }

    public static String getTransferSyntax(Dataset source) {
        FileMetaInfo fmi = source.getFileMetaInfo();
        return fmi != null ? fmi.getTransferSyntaxUID() : UIDs.ImplicitVRLittleEndian;
    }

    @SuppressWarnings("rawtypes")
    protected BufferedImage createBufferedImage(int samples, int bitsAllocated, int columns, int rows) {
        int pixelStride;
        int[] bandOffset;
        int dataType;
        int colorSpace;
        if (samples == 3) {
            pixelStride = 3;
            bandOffset = new int[]{0, 1, 2};
            dataType = DataBuffer.TYPE_BYTE;
            colorSpace = ColorSpace.CS_sRGB;
        } else {
            pixelStride = 1;
            bandOffset = new int[]{0};
            dataType = bitsAllocated == 8 ? DataBuffer.TYPE_BYTE : DataBuffer.TYPE_USHORT;
            colorSpace = ColorSpace.CS_GRAY;
        }
        SampleModel sm = new PixelInterleavedSampleModel(dataType, columns, rows, pixelStride, columns * pixelStride, bandOffset);
        ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(colorSpace), sm.getSampleSize(), false, false, Transparency.OPAQUE, dataType);
        WritableRaster r = Raster.createWritableRaster(sm, new Point(0, 0));
        return new BufferedImage(cm, r, false, new Hashtable());
    }

    private void read(ByteOrder byteOrder, InputStream in, short[][] data) throws IOException {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            readLE(in, data);
        } else {
            readBE(in, data);
        }
    }

    private void readLE(InputStream in, short[][] data) throws IOException {
        int lo;
        int hi;
        for (int i = 0; i < data.length; i++) {
            short[] bank = data[i];
            for (int j = 0; j < bank.length; j++) {
                lo = in.read();
                hi = in.read();
                if ((lo | hi) < 0) {
                    throw new EOFException();
                }
                bank[j] = (short) ((lo & 0xff) + (hi << 8));
            }
        }
    }

    private void readBE(InputStream in, short[][] data) throws IOException {
        int lo;
        int hi;
        for (int i = 0; i < data.length; i++) {
            short[] bank = data[i];
            for (int j = 0; j < bank.length; j++) {
                hi = in.read();
                lo = in.read();
                if ((lo | hi) < 0) {
                    throw new EOFException();
                }
                bank[j] = (short) ((lo & 0xff) + (hi << 8));
            }
        }
    }

    private void read(InputStream in, byte[][] data) throws IOException {
        int read;
        for (int i = 0; i < data.length; i++) {
            byte[] bank = data[i];
            for (int toread = bank.length; toread > 0;) {
                read = in.read(bank, bank.length - toread, toread);
                if (read == -1) {
                    throw new EOFException("Length of pixel matrix is too short!");
                }
                toread -= read;
            }
        }
    }

    private SampleModel getSampleModel(int planarConfiguration, int columns, int dataType, int rows, int samples) {
        if (planarConfiguration == 0) {
            return new PixelInterleavedSampleModel(dataType, columns, rows, samples, columns * samples, samples == 1 ? GRAY_BAND_OFFSETS : RGB_BAND_OFFSETS);
        } else {
            return new BandedSampleModel(dataType, columns, rows, samples);
        }
    }

    private ColorModel getColorModel(int samples, int bitsUsed) {

        if (samples == 3) {
            return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{bitsUsed, bitsUsed, bitsUsed}, false, false, ColorModel.OPAQUE, dataType);
        } else {
            return new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_GRAY), new int[]{bitsUsed}, false, false, ColorModel.OPAQUE, dataType);
        }
    }

    private boolean isOverlayInPixelData(Dataset ds) {
        for (int i = 0; i < 16; ++i) {
        	try{
	            if (ds.getInt(Tags.OverlayBitPosition + 2 * i, 0) != 0) {
	                return true;
	            }
            }catch(Exception e){
            	log.warn("Error in getInt(Tags.OverlayBitPosition + 2 * i, 0) for this image.");
            }
        }
        return false;
    }

    private boolean initWriteParam(ImageWriteParam writerParam, String tsuid, Dataset ds, int samples) {

        int i = 0;

        if (tsuid.equalsIgnoreCase(UIDs.JPEG2000Lossless)) {
            i = 1;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEG2000Lossy)) {
            i = 2;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEGLSLossy)) {
            i = 3;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEGLSLossless)) {
            i = 4;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEGBaseline)) {
            i = 5;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEGLossless)) {
            i = 6;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEGExtended)) {
            i = 7;
        }
        if (tsuid.equalsIgnoreCase(UIDs.JPEGLossless14)) {
            i = 8;
        }
        switch (i) {
            case 1:
                {
                    if (samples == 3) {
                        ds.putUS(Tags.PlanarConfiguration, 0);
                        ds.putCS(Tags.PhotometricInterpretation, YBR_RCT);
                    }
                    if (writerParam instanceof J2KImageWriteParam) {
                        J2KImageWriteParam j2KwParam = (J2KImageWriteParam) writerParam;
                        log.debug("Writing a jpeg2000 image!");
                        j2KwParam.setWriteCodeStreamOnly(true);
                    }
                    return true;
                }
            case 2:
                {
                    if (samples == 3) {
                        ds.putUS(Tags.PlanarConfiguration, 0);
                        ds.putCS(Tags.PhotometricInterpretation, YBR_RCT);
                    }
                    if (writerParam instanceof J2KImageWriteParam) {
                        J2KImageWriteParam j2KwParam = (J2KImageWriteParam) writerParam;
                        log.debug("Writing a jpeg2000 image!");
                        j2KwParam.setWriteCodeStreamOnly(true);
                    }
                    return true;
                }
            case 3:
                {
                    if (samples == 3) {
                        ds.putUS(Tags.PlanarConfiguration, 0);
                    }
                    writerParam.setCompressionType(JPEG_LS);
                    return false;
                }
            case 4:
                {
                    if (samples == 3) {
                        ds.putUS(Tags.PlanarConfiguration, 0);
                    }
                    writerParam.setCompressionType(JPEG_LS);
                    return false;
                }
            case 5:
                {
                    return false;
                }
            case 6:
                {
                    writerParam.setCompressionType(JPEG_LOSSLESS);
                    if (samples == 3) {
                        ds.putUS(Tags.PlanarConfiguration, 0);
                    }
                    return false;
                }
            case 7:
                {
                    return false;
                }
            case 8:
                {
                    return false;
                }
            default:
                {
                    log.warn("Wrong Transfer Syntax UID.");
                    break;
                }
        }
        return false;
    }
}