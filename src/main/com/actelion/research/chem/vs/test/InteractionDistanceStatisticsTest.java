/*
 * Copyright (c) 2020.

 *
 *  This file is part of DataWarrior.
 *
 *  DataWarrior is free software: you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  DataWarrior is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with DataWarrior.
 *  If not, see http://www.gnu.org/licenses/.
 *
 *  @author Modest v. Korff
 *
 */

package com.actelion.research.chem.vs.test;

import com.actelion.research.chem.interactionstatistics.InteractionDistanceStatistics;

import java.io.IOException;

/**
 * InteractionDistanceStatisticsTest
 * <p>Modest v. Korff</p>
 * <p>
 * Created by korffmo1 on 05.03.20.
 */
public class InteractionDistanceStatisticsTest {


    public static void main(String[] args) throws IOException {

        InteractionDistanceStatistics interactionDistanceStatistics = InteractionDistanceStatistics.getInstance();

        interactionDistanceStatistics.readFromFile();



    }
}
