package org.redpill.alfresco.clamav.repo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;

public class AcavUtils {

  public static void closeQuietly(ResultSet resultSet) {
    try {
      resultSet.close();
    } catch (Exception ex) {
      // just swallow...
    }
  }

  public static File copy(InputStream inputStream) {
    ParameterCheck.mandatory("inputStream", inputStream);

    File tempFile = TempFileProvider.createTempFile("acav_tempfile_", ".tmp");

    OutputStream outputStream = null;

    try {
      outputStream = new FileOutputStream(tempFile);

      IOUtils.copy(inputStream, outputStream);

      return tempFile;
    } catch (Exception ex) {
      tempFile.delete();

      throw new AlfrescoRuntimeException(ex.getMessage(), ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(outputStream);
    }
  }

}
