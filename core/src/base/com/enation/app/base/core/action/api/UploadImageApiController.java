package com.enation.app.base.core.action.api;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.enation.app.base.core.upload.IUploader;
import com.enation.app.base.core.upload.UploadFacatory;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.util.FileUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.JsonUtil;

import net.sf.json.JSONArray;

/**
 * 图片上传API
 * @author kanon 2015-9-22 version 1.1 添加注释
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/base/upload-image")
public class UploadImageApiController  {

	/**
	 *  上传图片
	 * @param image 图片
	 * @param imageFileName 图片名称
	 * @param subFolder 存放文件夹名称
	 * @return 上传成功返回： 图片地址，失败返回上传图片错误信息
	 */
	@ResponseBody
	@RequestMapping(value="/upload-image")
	public Object uploadImage(MultipartFile file ,String  subFolder){
		try{
			if (file!=null) {
				if(!FileUtil.isAllowUpImg(file.getOriginalFilename())){
					return JsonResultUtil.getErrorJson("不允许上传的文件格式，请上传gif,jpg,bmp格式文件。");
				}else{
					InputStream stream=null;
					stream=file.getInputStream();
					IUploader uploader=UploadFacatory.getUploaer();
					String fsImgPath =uploader.upload(stream, subFolder, file.getOriginalFilename());
					//return "{\"img\":\""+StaticResourcesUtil.convertToUrl(fsImgPath)+"\",\"fsimg\":\""+fsImgPath+"\"}";
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("img", StaticResourcesUtil.convertToUrl(fsImgPath));
					map.put("fsimg", fsImgPath);
					
					String data=JsonUtil.MapToJson(map);
					String result="{\"result\":1,\"data\":"+data+"}";
					/**
					 * IE9以下上传非jpg格式图片出错修复_by jianghongyan
					 */
					return result;
				}
			}
		}catch(Exception e){

			return JsonResultUtil.getErrorJson("上传出错"+e.getLocalizedMessage());
		}
		return JsonResultUtil.getErrorJson("请重试");
	}

	/**
	 * 上传图片
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/upload-img")
	public  String uploadImg(MultipartFile upfile){
		InputStream stream=null;
		try {
			stream=upfile.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
		IUploader uploader=UploadFacatory.getUploaer();
		String path = uploader.upload(stream, "ueditor",upfile.getOriginalFilename());
		//System.out.println(UploadUtil.replacePath(path));

		Map<String,Object> imgMap = new HashMap<String,Object>();
		imgMap.put("state", "SUCCESS");
		imgMap.put("url", StaticResourcesUtil.convertToUrl(path));
		imgMap.put("title", "show.jpg");
		imgMap.put("original", "show.jpg");

		String con = JSONArray.fromObject(imgMap).toString();
		String configJson = con.substring(1, con.length()-1);

		return configJson;
	}
	/**
	 *  上传图片
	 * @param image 图片
	 * @param imageFileName 图片名称
	 * @param subFolder 存放文件夹名称
	 * @return 上传成功返回： 图片地址，失败返回上传图片错误信息
	 */
	@ResponseBody
	@RequestMapping(value="/upload")
	public Object upload(MultipartFile image ,String  subFolder){
		try{
			if (image!=null) {
				if(!FileUtil.isAllowUpImg(image.getOriginalFilename())){
					return JsonResultUtil.getErrorJson("不允许上传的文件格式，请上传gif,jpg,bmp格式文件。");
				}else{
					InputStream stream=null;
					try {
						stream=image.getInputStream();
					} catch (Exception e) {
						e.printStackTrace();
					}
					IUploader uploader=UploadFacatory.getUploaer();
					String fsImgPath =uploader.upload(stream, subFolder, image.getOriginalFilename());
					return "{\"img\":\""+StaticResourcesUtil.convertToUrl(fsImgPath)+"\",\"fsimg\":\""+fsImgPath+"\"}";
				}

			}


		}catch(Throwable e){
			return JsonResultUtil.getErrorJson("上传出错"+e.getLocalizedMessage());
		}
		return JsonResultUtil.getErrorJson("请重试");
	}

	@ResponseBody
	@RequestMapping(value="/sd", produces =MediaType.APPLICATION_JSON_VALUE)
	public Object dsss(){
		return "asdd";
	}
}
