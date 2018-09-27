FROM node:8.12-alpine
RUN npm install -g yarn
RUN mkdir /opt/app
COPY package.json yarn.lock /opt/app/ 
WORKDIR /opt/app
RUN yarn install
COPY . /opt/app
CMD ["node", "index.js"]

