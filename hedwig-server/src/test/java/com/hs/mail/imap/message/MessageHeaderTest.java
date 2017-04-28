package com.hs.mail.imap.message;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.hs.mail.test.TestUtil;

public class MessageHeaderTest {

	@Test
	public void test() throws IOException {
		InputStream is = TestUtil.readResourceAsStream("/quotedPrintableWithAttach.eml");
		MessageHeader header = new MessageHeader(is);
		assertNotNull(header);
		assertEquals(header.getSubject(), "��(HB)20110922-002 Failed to load IMAP envelope �� ���� �����Դϴ�.");
		assertEquals(header.getFrom(), "���ر� <highbase@secuace.co.kr>");
		assertEquals(header.getFromAddress(), "highbase@secuace.co.kr");
	}

	@Test
	public void testBrokenSubject() throws Exception {
		MessageHeader header = newMessageHeader("/brokenSubject1.eml");
		assertEquals(header.getSubject(), "Re: [5/15]�뼺���� PJT ������� ���Ϻ��� - �谭��");
		assertEquals(header.getFrom(), "����� <xevi@handysoft.co.kr>");

		header = newMessageHeader("/brokenSubject2.eml");
		assertEquals(header.getSubject(), "[��û] ������ ���� ��û ���� Ȯ�ο� ���� ����2");
		assertEquals(header.getFrom(), "��ī�� <pika@iris4.handysoft.co.kr>");

		header = newMessageHeader("/brokenSubject3.eml");
		assertEquals(header.getSubject(), "������ �����Ǿ����ϴ�-��������+���Ͼ˸�(�ϰ�)_����");
		assertEquals(header.getFrom(), "��ī�� <pika@iris4.handysoft.co.kr>");

		header = newMessageHeader("/brokenSubject4.eml");
		assertEquals(header.getSubject(), "[����] ZETA ICS ���������� �ý��� ���� �����Ͽ� �������� ��û�帳��??.");
		assertEquals(header.getFrom(), "����� <nonstop@dseltec.co.kr>");
		
		header = newMessageHeader("/brokenSubject5.eml");
		assertEquals(header.getSubject(), "Re: [�˻� ǥ�ؼҽ�] UI ������ ���� �������縦 �ǽ��ϰ��� �մϴ�.");
		assertEquals(header.getFrom(), "youngho.kim <youngho.kim@konantech.com>");

		header = newMessageHeader("/asciiHeader.eml");
		assertEquals(header.getSubject(), "Please verify that we have the right address for you");
		assertEquals(header.getFrom(), "Apple <appleid@id.apple.com>");

		header = newMessageHeader("/notAsciiKorean.eml");
		assertEquals(header.getSubject(), "[�ٳ�] �޴��� ���� Ȯ�� �����Դϴ�.");
		assertEquals(header.getFrom(), "�ٳ�<paymail@paymail.danal.co.kr>");

		header = newMessageHeader("/notAsciiMixed.eml");
		assertEquals(header.getSubject(), "Re: Big Data ���� New Biz ���̵�� ���� ���� ȸ�Ƿ�(2013-12-01)");
		assertEquals(header.getFrom(), "�̹��� <mklee@konantech.com>");
	}

	private static MessageHeader newMessageHeader(String resource)
			throws IOException {
		InputStream is = TestUtil.readResourceAsStream(resource);
		return new MessageHeader(is);
	}

}
