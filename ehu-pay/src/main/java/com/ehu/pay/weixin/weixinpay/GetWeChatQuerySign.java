package com.ehu.pay.weixin.weixinpay;

import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

import com.ehu.pay.config.EhPayConfig;
import com.ehu.pay.constants.BaseConstants;
import com.ehu.pay.constants.PayResultCodeConstants;
import com.ehu.pay.constants.PayResultMessageConstants;
import com.ehu.pay.exception.PayException;
import com.ehu.pay.util.StringUtils;
import com.ehu.pay.weixin.util.MD5Util;
import com.ehu.pay.weixin.util.WeChatUtils;



/**
 * 
 * @author AlanSun
 * 2016-08-10
 * 微信查询订单类
 */
@Slf4j
public class GetWeChatQuerySign {
	static final String requestUrl = "https://api.mch.weixin.qq.com/pay/orderquery";
	
	@SuppressWarnings("unchecked")
	public static Object getQuertResult(String out_trade_no ,String queryFlag) throws PayException{
		EhPayConfig config = EhPayConfig.getInstance();
		Random random = new Random();
		String nonce_str = MD5Util.MD5Encode(String.valueOf(random.nextInt(10000)), "gbk");
		SortedMap<String, String> packageParams = new TreeMap<String,String>();
		packageParams.put("appid", config.getWxPay_appid());
		packageParams.put("mch_id", config.getWxPay_mch_id());
		packageParams.put("nonce_str", nonce_str);
		packageParams.put("out_trade_no", out_trade_no);
		SortedMap<String, String> map = WeChatUtils.createSign(packageParams, config);
		Map<String,String> resultMap = WeChatUtils.getResponseInfo(map,requestUrl);
		Object flag = judgeOrderState(resultMap,queryFlag);
		return flag;
	}
	/**
	 * 判断订单状态
	 * @param resultMap
	 * @param queryFlag
	 * @return
	 * @throws PayException
	 */
	protected static Object judgeOrderState(Map<String ,String> resultMap,final String queryFlag) throws PayException{
		if(resultMap.containsKey("return_code") && BaseConstants.RETURN_SUCCESS.equals(resultMap.get("return_code"))){
			if(resultMap.containsKey("result_code") && BaseConstants.RETURN_SUCCESS.equals(resultMap.get("result_code"))){
				String resultCode = resultMap.get("result_code");
				if(BaseConstants.RETURN_SUCCESS.equals(resultCode)){
					if(StringUtils.isEmpty(queryFlag)){
						return resultMap.get("trade_state");
					}else{
						String tradeState = resultMap.get("trade_state");
						if(BaseConstants.TRADE_STATE_SUCCESS.equals(queryFlag)){
							if(BaseConstants.TRADE_STATE_SUCCESS.equals(tradeState)){
								return true;
							}else{
								GetWeChatQuerySign.checkTradeState(tradeState);
							}
						}
					}
				}
			}else{
				log.error("查询微信服务器错误" + resultMap);
				throw new PayException(PayResultCodeConstants.ERROR_CODE_WECHATPAY_10003, resultMap.get("err_code_des"));
			}
		}else{
			log.error("查询微信服务器错误");
			throw new PayException(PayResultCodeConstants.ERROR_CODE_WECHATPAY_10003, PayResultMessageConstants.STRING_WECHATPAY_10003);
		}
		return false;
	}
	
	protected static void checkTradeState(String tradeState) throws PayException{
		if(BaseConstants.TRADE_STATE_SUCCESS.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_SUCCESS_30007, PayResultMessageConstants.TRADE_STATE_SUCCESS_30007);
		}else if(BaseConstants.TRADE_STATE_NOTPAY.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_NOTPAY_30006, PayResultMessageConstants.TRADE_STATE_NOTPAY_30006);
		}else if(BaseConstants.TRADE_STATE_REFUND.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_REFUND_30008, PayResultMessageConstants.TRADE_STATE_REFUND_30008);
		}else if(BaseConstants.TRADE_STATE_CLOSED.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_CLOSED_30009, PayResultMessageConstants.TRADE_STATE_CLOSED_30009);
		}else if(BaseConstants.TRADE_STATE_USERPAYING.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_USERPAYING_30010, PayResultMessageConstants.TRADE_STATE_USERPAYING_30010);
		}else if(BaseConstants.TRADE_STATE_PAYERROR.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_PAYERROR_30011, PayResultMessageConstants.TRADE_STATE_PAYERROR_30011);
		}else if(BaseConstants.TRADE_STATE_REVOKED.equals(tradeState)){
			throw new PayException(PayResultCodeConstants.TRADE_STATE_REVOKED_30012, PayResultMessageConstants.TRADE_STATE_REVOKED_30012);
		}
	}
}
