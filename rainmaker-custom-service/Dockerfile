FROM egovio/alpine-node-builder-10:yarn AS build
RUN mkdir /opt/app
COPY package.json yarn.lock /opt/app/ 
WORKDIR /opt/app
RUN yarn install
COPY . /opt/app
CMD ["node", "index.js"]

