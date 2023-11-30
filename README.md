# KNIME® Text Processing

[![Jenkins](https://jenkins.knime.com/buildStatus/icon?job=knime-textprocessing%2Fmaster)](https://jenkins.knime.com/job/knime-textprocessing/job/master/)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=KNIME_knime-textprocessing&metric=alert_status&token=55129ac721eacd76417f57921368ed587ad8339d)](https://sonarcloud.io/summary/new_code?id=KNIME_knime-textprocessing)

This repository is maintained by the [KNIME Team Rakete](mailto:team-rakete@knime.com).

[KNIME Analytics Platform - Text Processing Integration](https://www.knime.com/knime-text-processing) is designed to read, enrich, manipulate, and extract textual data, and transform it into numerical representations, such as document or term vectors. Once numerical vectors are created, regular KNIME nodes can be applied, for example, for predictive modeling, clustering analysis, or visualization.

### Content
This repository contains the source code for KNIME - Text Processing Integration. The code is organized as follows:

* _org.knime.ext.textprocessing_: Text Processing integration nodes and data types
* _org.knime.ext.textprocessing.dl4j_: [Text Processing Word2Vec](https://tech.knime.org/deeplearning4j-textprocessing) integration
* _org.knime.ext.textprocessing.models_: External models for tokenization and tagging (NER, POS, etc.)

### Development Notes
You can find instructions on how to work with our code or develop extensions for KNIME Analytics Platform in the _knime-sdk-setup_ repository on [BitBucket](https://bitbucket.org/KNIME/knime-sdk-setup) or [GitHub](http://github.com/knime/knime-sdk-setup).

### Join the Community!
* [KNIME Forum](https://tech.knime.org/forum/knime-textprocessing)
