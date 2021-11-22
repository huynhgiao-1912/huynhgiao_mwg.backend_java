package mwg.wb.pkg.product;

import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.imageio.ImageIO;

import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import mwg.wb.client.elasticsearch.ElasticClientWrite;
import mwg.wb.client.graph.ORThreadLocal;
import mwg.wb.common.IMessage.DataAction;
import mwg.wb.common.Ididx;
import mwg.wb.common.Logs;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;
import mwg.wb.model.api.ClientConfig;
import mwg.wb.model.search.GallerySO;

public class ProductUserGallery implements Ididx {

	private ORThreadLocal factoryRead = null;
	private String CurrentIndexDB = "";
	private ClientConfig config = null;

	@Override
	public void InitObject(ObjectTransfer objectTransfer) {
		factoryRead = (ORThreadLocal) objectTransfer.factoryRead;
		config = (ClientConfig) objectTransfer.clientConfig;
		CurrentIndexDB = config.ELASTICSEARCH_PRODUCT_GALLERY_INDEX;
	}

	@Override
	public ResultMessage Refresh(MessageQueue message) {
//		int DataCenter = message.DataCenter;
		int PictureID = Integer.parseInt(message.Identify);

		String strNOTE = message.Note + "";
		boolean isLog = false;
		if (strNOTE.contains("LOG")) {
			isLog = true;
		}

		ResultMessage r = new ResultMessage();
		r.Code = ResultCode.Success;
		Logs.Log(isLog, strNOTE, "<--------------Refresh--------------- "+PictureID);
		 
		if (message.Action != DataAction.Delete) {
			GallerySO[] solist = null;
			try {
				solist = factoryRead.QueryFunction("product_GetGallerySOByPictureID", GallerySO[].class, false,
						PictureID);
			} catch (Throwable e1) {
				r.Code = ResultCode.Retry;
				Logs.LogException(e1);
				r.Message = "index fail";
				Logs.Log(isLog, strNOTE, "index fail Retry" );
				
				return r;
			}
			if (solist == null) { // không thể nào trả ra null, chỉ có thể do lỗi query
				r.Message = "null solist, retrying...";
				r.Code = ResultCode.Retry;
				Logs.Log(isLog, strNOTE, "null solist, retrying..." );
				return r;
			}
			GallerySO so;
			if (solist.length == 0) {
//				r.Code = ResultCode.Error;
//				r.Message = "Picture #" + PictureID + " does not exist";
				
			 
				Logs.Log(isLog, strNOTE, "deletegallerry.." );
				return doDelete(message, r, isLog, strNOTE);
			}
			Logs.Log(isLog, strNOTE, "so = solist[0];");
			so = solist[0];
			if (so == null) { 
				Logs.Log(isLog, strNOTE, "so == null.." );
				 
			}
//			so.CreatedDateLongValue = so.CreatedDate.getTime();
			getImageSize(isLog, strNOTE, so);
			try {

				if (!ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).IndexObject(CurrentIndexDB,
						so, so.pictureid + "")) {
					r.Code = ResultCode.Retry;
					Logs.Log(isLog, strNOTE,
							"-Index gallery status to ES FAILED: " + PictureID + " ######################");
					r.Message = "index fail";
					Logs.Log(isLog, strNOTE, "Index gallery status to ES FAILED retry.." );
					return r;
				}

			} catch (Exception e) {
				r.Code = ResultCode.Retry;
				Logs.Log(isLog, strNOTE, "Exception Index gallery status to ES FAILED: " + PictureID);
				Logs.LogException(e);
				r.Message = "index exception";
				r.StackTrace = Logs.GetStacktrace(e);
				
			} finally {

			}
		} else {
			
			Logs.Log(isLog, strNOTE, "doDelete." );
			
			return doDelete(message, r, isLog, strNOTE);
		}
		return r;
	}

	private ResultMessage doDelete(MessageQueue message, ResultMessage r, boolean log, String note) {
		int PictureID = Integer.parseInt(message.Identify);
		try {
			var update = new UpdateRequest(CurrentIndexDB, PictureID + "").doc("{\"isdeleted\":1}", XContentType.JSON)
					.docAsUpsert(true).detectNoop(false);
			var client = ElasticClientWrite.getInstance(config.SERVER_ELASTICSEARCH_WRITE_HOST).getClient();
			var response = client.update(update, RequestOptions.DEFAULT);
			if (response == null || response.getResult() != Result.UPDATED) {
				r.Code = ResultCode.Retry;
				Logs.WriteLine("-Delete gallery status to ES FAILED: " + PictureID + ", " + response.getResult().name()
						+ "######################");
				r.Message = "delete fail";
			
				return r;
			}
			r.Code = ResultCode.Success;
		} catch (Exception e) {
			r.Code = ResultCode.Retry;
			Logs.Log(log, note, "Exception delete gallery status to ES FAILED: " + PictureID);
			Logs.LogException(e);
			r.StackTrace = Logs.GetStacktrace(e);
			r.Message = "delete exception";
			
		}
		return r;
	}

	public void getImageSize(boolean isLog, String StrNote, GallerySO input) {
//		String cdnurl = "file://192.168.2.111/image/";
		Logs.WriteLine("<getImageSize");
		String cdnurl = "https://cdn.tgdd.vn/";
		String folder = null;
		switch (input.picturetype) {
		case 3:
			if (input.categoryid > 0 && input.productid > 0)
				folder = cdnurl + "UserUpload/" + input.categoryid + "/" + input.productid + "/";
			else
				folder = cdnurl + "UserUpload/Uncate/";
			break;
		case 2:
			folder = cdnurl + "UserUpload/Selfie/" + (input.productid > 0 ? input.productid : "Uncate") + "/";
			break;
		default:
			String type = null;
			switch (input.imagetype) {
			case 2:
				type = "CameraGallery";
				break;
			case 3:
				type = "os";
				break;
			case 6:
			case 13:
				type = "selfie";
				break;
			case 10:
			case 11:
				type = "compare";
				break;
			default:
				break;
			}
			if (folder == null)
				folder = cdnurl + "Products/Images/" + input.categoryid + "/" + input.productid + "/"
						+ (type == null ? "" : type + "/");
			if (input.picture != null) {
				try {
					if (input.width <= 0) {
						Logs.Log(isLog, StrNote, "file 1" + folder + input.picture);
						var pic = ImageIO.read(new URL(folder + input.picture));
						if (pic != null) {
							input.width = pic.getWidth();
							input.height = pic.getHeight();
						}

						Logs.Log(isLog, StrNote, "input.width1 " + input.width);
					}
				} catch (Throwable e) {
					Logs.Log(isLog, StrNote, "ImageIO.read1" + Logs.GetStacktrace(e));
//					Logs.LogException(e);
				}
			} else {
				Logs.Log(isLog, StrNote, "null picture ");
				Logs.getInstance().Log(isLog, StrNote, "getImageSize null picture", input);
			}
			if (input.picturelarge != null) {
				try {
					if (input.widthlarge <= 0) {
						Logs.Log(isLog, StrNote, "file 2" + folder + input.picturelarge);
						var large = ImageIO.read(new URL(folder + input.picturelarge));
						if (large != null) {
							input.widthlarge = large.getWidth();
							input.heightlarge = large.getHeight();
						}
						Logs.Log(isLog, StrNote, "input.widthlarge 2" + input.widthlarge);
					}
				} catch (Throwable e) {
					Logs.Log(isLog, StrNote, "ImageIO.read2" + Logs.GetStacktrace(e));
				}
			} else
				Logs.Log(isLog, StrNote, "null picture large");
			if (input.pictureorg != null) {
				try {
					if (input.widthorg <= 0) {
						Logs.Log(isLog, StrNote, "file 3" + folder + input.pictureorg);
						var org = ImageIO.read(new URL(folder + input.pictureorg));
						if (org != null) {
							input.widthorg = org.getWidth();
							input.heightorg = org.getHeight();
						}
						Logs.Log(isLog, StrNote, "input.widthorg 3 " + input.widthorg);
					}
				} catch (Throwable e) {
					Logs.Log(isLog, StrNote, "ImageIO.read3" + Logs.GetStacktrace(e));
				}

			} else
				Logs.Log(isLog, StrNote, "null picture org");
			break;
		}
		Logs.WriteLine("getImageSize>");
	}

	@Override
	public ResultMessage RunScheduleTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
