FROM egovio/ci-react:v8.4.0
RUN npm install yarn
RUN mkdir /opt/app
COPY index.js  package.json yarn.lock /opt/app/ 
COPY ./templates /opt/app/templates
COPY ./utils /opt/app/utils
WORKDIR /opt/app
RUN apt-get purge -y wget subversion ca-certificates bzip2 geoip-bin mercurial mysql-common  x11-common
RUN yarn -y init
RUN yarn add express  pg-promise
CMD ["node", "index.js"]

