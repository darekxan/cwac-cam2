/**
 * Copyright (c) 2015 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2.plugin;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.util.Log;

import com.commonsware.cwac.cam2.CameraConfigurator;
import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraPlugin;
import com.commonsware.cwac.cam2.CameraSession;
import com.commonsware.cwac.cam2.ClassicCameraConfigurator;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.SimpleCameraTwoConfigurator;
import com.commonsware.cwac.cam2.SimpleClassicCameraConfigurator;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Plugin for managing flash modes
 */
public class FlashModePlugin implements CameraPlugin {
  private final List<FlashMode> flashModes;
  private FlashMode selectedFlashMode;

  public FlashModePlugin(List<FlashMode> flashModes) {
    EventBus.getDefault().register(this);
    this.flashModes=flashModes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends CameraConfigurator> T buildConfigurator(Class<T> type) {
    if (type == ClassicCameraConfigurator.class) {
      return (type.cast(new Classic()));
    }

    return(type.cast(new Two()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate(CameraSession session) {
    // no validation required
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() {
    EventBus.getDefault().unregister(this);
  }

  @SuppressWarnings("unused")
  public void onEvent(CameraController.FlashModeRequestEvent event) {
    selectedFlashMode = event.getMode();
    EventBus.getDefault().post(new CameraEngine.FlashModeChangedEvent());
  }

  class Classic extends SimpleClassicCameraConfigurator {
    /**
     * {@inheritDoc}
     */
    @Override
    public Camera.Parameters configureStillCamera(
      Camera.CameraInfo info,
      Camera camera, Camera.Parameters params) {
      if (params!=null) {
        if (flashModes.contains(selectedFlashMode)) {
          params.setFlashMode(selectedFlashMode.toClassic());
        } else {
            Log.w("CWAC-Cam2", "no support for requested flash mode");
        }
      }
      return(params);
    }
  }


  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  class Two extends SimpleCameraTwoConfigurator {
    /**
     * {@inheritDoc}
     */
    @Override
    public void addToCaptureRequest(CameraCharacteristics cc,
                                    boolean facingFront,
                                    CaptureRequest.Builder captureBuilder) {
      int desiredMode=getConvertedFlashMode(cc);

      if (desiredMode==-1) {
        Log.w("CWAC-Cam2", "no support for requested flash mode");
      }
      else {
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
          desiredMode);
      }
    }

    @Override
    public void addToPreviewRequest(CameraCharacteristics cc,
                                    CaptureRequest.Builder captureBuilder) {
      int desiredMode=getConvertedFlashMode(cc);

      if (desiredMode==-1) {
        Log.w("CWAC-Cam2", "no support for requested flash mode");
      }
      else {
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
          desiredMode);
      }
    }

    private int getConvertedFlashMode(CameraCharacteristics cc) {
      int[] availModes=cc.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES);
// TODO CAMERA2
//      for (FlashMode mode : flashModes) {
//        int desiredMode=-1;
//
//        if (mode==FlashMode.OFF) {
//          desiredMode=CameraMetadata.CONTROL_AE_MODE_ON;
//        }
//        else if (mode==FlashMode.ON) {
//          desiredMode=CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH;
//        }
//        else if (mode==FlashMode.AUTO) {
//          desiredMode=CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH;
//        }
//        else if (mode==FlashMode.REDEYE) {
//          desiredMode=CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE;
//        }
//
//        for (int availMode : availModes) {
//          if (desiredMode==availMode) {
//            return(desiredMode);
//          }
//        }
//      }

      return(-1);
    }
  }
}
