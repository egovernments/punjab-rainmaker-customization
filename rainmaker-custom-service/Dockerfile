FROM node:8.12-alpine
RUN mkdir /opt/app
RUN npm install -g yarn
COPY package.json yarn.lock /opt/app/ 
WORKDIR /opt/app
RUN yarn install
COPY . /opt/app
CMD ["node", "index.js"]

