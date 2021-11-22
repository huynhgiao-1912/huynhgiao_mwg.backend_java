package mwg.wb.pkg.product;

import mwg.wb.common.Ididx;
import mwg.wb.common.MessageQueue;
import mwg.wb.common.ObjectTransfer;
import mwg.wb.common.ResultMessage;
import mwg.wb.common.ResultMessage.ResultCode;

public class Product implements Ididx {

	private ObjectTransfer objectTransfer = null;

	@Override
	public void InitObject(ObjectTransfer aobjectTransfer) {
		objectTransfer = aobjectTransfer;
	}

	public ResultMessage Refresh(MessageQueue message) {
		ResultMessage resultMessage = new ResultMessage();
		resultMessage.Code = ResultCode.Success;
		if (message.ClassName.equals("mwg.wb.pkg.product.SeoUrl")) {
			SeoUrl seourl = new SeoUrl();
			seourl.InitObject(objectTransfer);
			return seourl.Refresh(message);
		}else if (message.ClassName.equals("ms.productse.ProductSE")) {
			var se = new mwg.wb.pkg.productse.ProductSE();
			se.InitObject(objectTransfer);
			return se.Refresh(message);
		} else if (message.ClassName.equals("mwg.wb.pkg.product.ProductUserGallery")) {
			ProductUserGallery gallery = new ProductUserGallery();
			gallery.InitObject(objectTransfer);
			return gallery.Refresh(message);
		}
		return resultMessage;

	}

	@Override
	public ResultMessage RunScheduleTask() {
		return null;
	}
}
