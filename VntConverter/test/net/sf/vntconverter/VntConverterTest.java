package net.sf.vntconverter;

/***********************************************************************************
 VntConverter - Converts Samsung Wave S8500 memo files to text files and vice versa

 Copyright (C) 2012  Uwe Damken

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program; if not, write to the Free Software Foundation, Inc.,
 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 **********************************************************************************/

import org.junit.Assert;
import org.junit.Test;

public class VntConverterTest {

	@Test
	public void test() {
		VntConverter converter = new VntConverter();
		{
			String in = "A=0D=0AB=0D=0AC=0D=0A=C3=84=0D=0A=0D=0AAaa=0D=0ADabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNO";
			String decoded = converter.decode(in);
			String encoded = converter.encode(decoded);
			Assert.assertEquals("re-encoded and original string length must equal", in.length(),
					encoded.length());
			Assert.assertEquals("re-encoded and original string must equal", in, encoded);
		}
		{
			String in = "AbcdefghijklmAbcdefghijklmnopqrstuvwxyznoAbcdefghijklmn-:'\",?opqrstuvwxyzAbcdefgh!@#$%^&*()ijklmnopqrstuvw+_/=3D<>{}[]xyzpAbcdefghijklm=C2=A1`~\\|=C2=B1=C3=B7=E2=80=A2=E2=89=AA=E2=89=ABnopqrstuvwxyzqr=C2=A3=E2=82=AC=C2=A5=E2=82=A9=C2=A7=E2=80=98=C2=BFstuvwxyz=C3=A4=C3=B6=C3=BC=C3=84=C3=96=C3=9C=C3=9FAbcdefghijklmnopqrstuvwxyz";
			String decoded = converter.decode(in);
			String encoded = converter.encode(decoded);
			Assert.assertEquals("re-encoded and original string length must equal", in.length(),
					encoded.length());
			Assert.assertEquals("re-encoded and original string must equal", in, encoded);
		}
	}

}
