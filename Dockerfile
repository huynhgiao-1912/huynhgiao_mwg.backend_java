FROM openjdk:11
LABEL author.name="Thanh Phi" \
  author.email="tuonglaivinhhang1@gmail.com"

RUN mkdir -p /root/app/mwg.factory

WORKDIR /root/app/mwg.factory/

COPY workerconfig/* ./
RUN mkdir -p soapxml
COPY workerconfig/soapxml/* ./soapxml/
COPY mwg.wb.factory.jar ./

WORKDIR /root/app/mwg.factory/soapxml/
RUN ls -lh
WORKDIR /root/app/mwg.factory/

RUN ls -lh

CMD ["java","-Xmx4G","-jar","mwg.wb.factory.jar", "-i", "didxnews"]




