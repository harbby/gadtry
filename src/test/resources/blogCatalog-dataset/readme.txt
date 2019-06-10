/*
 * Copyright (C) 2018 The GadTry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
Social Computing Data Repository - Basic Information
==========================================================================
Dataset Name: BlogCatalog
Abstract: BlogCatalog is the social blog directory which manages the bloggers and their blogs.
Number of Nodes: 88,784
Number of Edges: 4,186,390
Missing Values: No

Source:
==========================================================================
Nitin Agarwal+, Xufei Wang*, Huan Liu*

+ Department of Information Science, University of Arkansas at Little Rock. E-mail:nxagarwal@ualr.edu

* School of Computing, Informatics and Decision Systems Engineering, Arizona State University. E-mail: huan.liu@asu.edu, xufei.wang@asu.edu

Data Set Information:
==========================================================================
[I]. Brief description
This is the data set crawled on July, 2009 from BlogCatalog ( http://www.blogcatalog.com ). BlogCatalog is a social blog directory website. 
This contains the friendship network crawled. For easier understanding, all the contents are organized in CSV file format.

[II]. Basic statistics
Number of bloggers : 88,784
Number of friendship pairs: 4,186,390

[III]. The data format

2 files are included:

1. nodes.csv
-- it's the file of all the users. This file works as a dictionary of all the users in this data set. It's useful for fast reference. It contains
all the node ids used in the dataset

2. edges.csv
-- this is the friendship network among the bloggers. The blogger's friends are represented using edges. Here is an example. 

1,2

This means blogger with id "1" is friend with blogger id "2".

Relevant Papers:
==========================================================================

1. Nitin Agarwal, Huan Liu, Sudheendra Murthy, Arunabha Sen, and Xufei Wang. "A Social Identity Approach to Identify Familiar Strangers in a Social Network", 3rd International AAAI Conference on Weblogs and Social Media (ICWSM09), pp. 2 - 9, May 17-20, 2009. San Jose, California. 