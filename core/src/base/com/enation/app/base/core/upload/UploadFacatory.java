package com.enation.app.base.core.upload;

import org.springframework.stereotype.Component;

import com.enation.app.base.core.model.ClusterSetting;
import com.enation.framework.context.spring.SpringContextHolder;
@Component
public class UploadFacatory {

	private UploadFacatory(){}
	
	
	/**
	 * 上传图片
	 * @return
	 */
	public static IUploader getUploaer(){
		IUploader uploade =(IUploader)SpringContextHolder.getBean("localUploader");
		int isopen =ClusterSetting.getFdfs_open();
		if(isopen==1){
			return new FastDFSUploader();
		}
		return uploade;
	}


}
