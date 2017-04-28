package com.hs.mail.imap.message;

import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.junit.Test;

public class DecodeTest {

	@Test
	public void testMain() {
		String[] strings = {
				"=?EUC-KR?Q?=BF=AC(HB)20110805-003[hedwig]_SMTP=C1=D7=B4=C2=B9=AE=C1=A6?=",
				"=?utf-8?B?UmU6Tm8gU3ViamVjdA==?=",
				"=?EUC-KR?Q?=BF=AC(HB)20110916-003_[hed?= wig] �� ���ǻ��� ó����Ȳ �� �繮��",
				"=?EUC-KR?Q?=BF=AC(HB)20110922-002_Failed_to_l?= oad IMAP envelope �� ���� �����Դϴ�.",
				"JS1(HB)20120308-003 [ȸ =?EUC-KR?Q?=C0=C7]_3/8_=B8=DE=C0=CF=BC=BA=B4=C9?=  ���� �� - ȸ�Ƿ�",
				"[IRIS] [01]������_����� =?UTF-8?Q?=EC=9B=90=EC=8B=A4_>_PO=5F2013.12.13?=",
				"[ZEUS] Commented: (SGBOARD-4588) 15 =?UTF-8?Q?=EB=85=84_=ED=95=98=EB=B0=98?= =?UTF-8?Q?=EA=B8=B0_=EC=A0=95=EB=B3=B4=EC=9D=B8?= =?UTF-8?Q?=ED=94=84=EB=9D=BC_=EB=8F=84=EC=9E=85_=EC=82=AC=EC=97=85_?= =?UTF-8?Q?=EC=9A=94=EA=B5=AC=EC=82=AC=ED=95=AD?=  ���� �� �η� �غ� ��û"};
		for (String encoded : strings) {
			String decoded = DecoderUtil.decodeEncodedWords(encoded, DecodeMonitor.SILENT);
			System.out.println(decoded);
		}
	}

}
