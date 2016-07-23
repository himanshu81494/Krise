package application.com.krise.utils;

/**
 * Created by dell on 16-Jul-16.
 */

    public interface UploadManagerCallback {

        public void uploadFinished(int requestType, int userId, int objectId, Object data, int uploadId, boolean status, String stringId);

        public void uploadStarted(int requestType, int objectId, String stringId, Object object);


    }

