#!/bin/python2.7

import feedparser
import json
import os
import requests
import time
from redisworks import Root

bot_chan_url = "https://app2.bot-chan.com/api"
api_key = os.environ['RSS_API_KEY']


root = Root(host='redis', port=6379, db=0)
#root.feeds = [{}]
if root.feeds == None:
    root.feeds = [{}]


def parse_all_known_feeds():
    print(root.feeds)
    new_feeds = []
    for feed in root.feeds:
        if feed == '' or feed == None or feed == {}:
            continue
        else:
            new_feeds.append(parse_feed(feed))
    if new_feeds != []:
        root.feeds = new_feeds


def parse_feed(feed):
    print("parse feed: " + str(feed))
    print("parsed feed items: " + str(feed['parsed_items']))

    items = feedparser.parse(feed['feed_url'])['items']
    for item in items:
        url = item['url'] if 'url' in item else item['link']
        parsed_items = feed['parsed_items'] if 'parsed_items' in feed else []
        if url not in parsed_items:
            parsed_items.append(url)
            consume_feed_item(feed['feed_url'], item, feed['platform_users'])
        feed['parsed_items'] = parsed_items

    return feed


def consume_feed_item(feed_url, item, platform_users):
    for message_id in platform_users:
        print(str(message_id) + " --- " + item['title'])
        headers = {'content-type': 'application/json'}
        message = "New item '%s' from feed: %s" % (item['title'], feed_url)
        requests.put((bot_chan_url + "/directReply/%s?apikey=%s&message=%s") % (message_id, api_key, message),
                headers=headers)


def check_messages():
    try:
        headers = {'content-type': 'application/json'}
        matchers = ['(?i)watch feed http(|s)://.*']
        payload = {'matchers': matchers, 'platform': 'RSS'}
        response = requests.post((bot_chan_url + "/uniqueMessages?apikey=%s") % api_key,
                headers=headers,
                data=json.dumps(payload))
        print(response.json())
        for data in response.json():
            data = response.json()[0]

            message = data['message']
            message_id = data['id']
            print(message)
            feed_url = message.split(' ')[2]
            print("Feed URL: " + feed_url)

            add_platform_user_to_feed(feed_url, message_id)
            reply_add_success(message_id)
    except Exception as e:
        print(e)
        pass


def reply_add_success(message_id):
    message = "New feed added!"
    headers = {'content-type': 'application/json'}
    payload = {'originalMessageID': message_id, 'message': message}
    requests.put((bot_chan_url + "/reply?apikey=%s") % api_key,
            headers=headers,
            data=json.dumps(payload))


def add_platform_user_to_feed(feed_url, platform_user):
    feeds = [x for x in root.feeds if x != {}]
    feed_item = None
    for feed_object in feeds:
        if (feed_object['feed_url'] == feed_url):
            feed_item = feed_object

    if feed_item is None:
        feed_item = {'feed_url': feed_url, 'platform_users': [], 'parsed_items': []}
        feeds.append(feed_item)
    if platform_user not in feed_item['platform_users']:
        feed_item['platform_users'].append(platform_user)

    root.feeds = feeds


if __name__ == "__main__":
    while True:
        check_messages()
        parse_all_known_feeds()
        time.sleep(5)
