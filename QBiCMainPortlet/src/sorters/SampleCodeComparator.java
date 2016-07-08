/*******************************************************************************
 * QBiC Project qNavigator enables users to manage their projects.
 * Copyright (C) "2016”  Christopher Mohr, David Wojnar, Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package sorters;


import java.util.Comparator;

import model.IBarcodeBean;

/**
 * Compares IBarcodeBeans by sample ID
 * @author Andreas Friedrich
 *
 */
public class SampleCodeComparator implements Comparator<IBarcodeBean> {

	private static final SampleCodeComparator instance = 
			new SampleCodeComparator();

	public static SampleCodeComparator getInstance() {
		return instance;
	}

	private SampleCodeComparator() {
	}

	@Override
	public int compare(IBarcodeBean o1, IBarcodeBean o2) {
		return o1.getCode().compareTo(o2.getCode());
	}

}
