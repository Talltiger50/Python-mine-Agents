
import requests
import json,time
block="OAK_LOG" #target block
# URL of the Spigot server hosting the plugin
url = "http://localhost:8000/jsoncommand"
aicount=0

# JSON data to send to the plugin
json_data = {
    "command": "deleteall",
    
}
try:
    print(requests.post(url, json=json_data))
except:
    pass

class block:
    def __init__(self,ID,Name):
        self.IDs=ID
        self.Names=Name
class inventory:
    def __init__(self,ID,Name):
        self.IDs=ID
        self.Names=Name

class Bot:
    def __init__(self,x,y,z)->None:
        global aicount
        self.index=0
        # JSON data to send to the plugin
        json_data = {
            "command": "makeai",
            "x":x+0.5,
            "y":y,
            "z":z+0.5
        }
        self.index=aicount
        aicount+=1
        # Send the JSON data to the plugin
        response = requests.post(url, json=json_data)
        print(response.text)
        
    # Print the response from the plugin
    def move(self,x,y,z)->None:
        # JSON data to send to the plugin
        json_data = {
            "command": "moveai",
            "x":float(x),
            "y":float(y),
            "z":float(z),
            "index":self.index
        }

        # Send the JSON data to the plugin
        requests.post(url, json=json_data)
        
    def getblocks(self,r)->block:
        # JSON data to send to the plugin
        json_data = {
            "command": "getblocks",
            "index":self.index
        }

        # Send the JSON data to the plugin
        response = requests.post(url, json=json_data)
        djson=json.loads(response.text)
        return block(djson["IDs"],djson["Names"])
    def getPos(self):
        # JSON data to send to the plugin
        json_data = {
            "command": "getpos",
            "index":self.index
        }

        # Send the JSON data to the plugin
        response = requests.post(url, json=json_data)
        djson=json.loads(response.text)
        return tuple(djson["pos"])
    def reset(self):
        # JSON data to send to the plugin
        json_data = {
            "command": "reset",
            "index":self.index
        }

        # Send the JSON data to the plugin
        requests.post(url, json=json_data)
    def message(self,msg):
        # JSON data to send to the plugin
        json_data = {
            "command": "message",
            "message":msg
        }

        # Send the JSON data to the plugin
        (requests.post(url, json=json_data))
    def block(self,x:int,y:int,z:int,type=True):
        # JSON data to send to the plugin
        json_data = {
            "command": "block",
            "index":self.index,
            "x":x,
            "y":y,
            "z":z,
            "type":type
        }

        # Send the JSON data to the plugin
        requests.post(url, json=json_data)
    def getitem(self)->inventory:
        # JSON data to send to the plugin
        json_data = {
            "command": "getitems",
            "index":self.index
            
        }

        # Send the JSON data to the plugin
        out=json.loads(requests.post(url, json=json_data).text)
        return inventory(out["ids"],out["names"])
        
players:list[Bot]=[]     

import os
import pickle
#import pygame
# /clone -93 4 45 -168 5 108 -168 4 182 /clone -93 1 45 -168 5 108 -168 4 182
# instantiate
# 46
import neat



# render text




def listtocoords(lis, size):
    lis2 = [[["" for x in range(size)] for y in range(size)] for z in range(size)]
    i = 0
    for z in range(size):
        for y in range(size):
            for x in range(size):
                lis2[z][y][x] = lis[i]
                i += 1
    return lis2


def listt(lis, num1, num2):
    l = []
    for i in range(num1, num2):
        l.append(lis[i])
    return l

def listbin(data:list[float])->int:
    new="0b"
    for i in data:
        if i>0:
            new+=str(1)
        else:
            new+=str(0)
    
    return int(new,2)

        

gen=0
def esttime(self, currntgen):
    return ((20 * self.size) * self.gens - currntgen) / 60

def eval_genomes(genomes, config):
    global gen
    ge = []
    fitnesses = []

    

    
    for i in players:
        i.reset()
    #self.runner.chat("/clone -93 4 45 -168 5 108 -168 4 182")

    nets = []
    for genome_id, genome in genomes:
        ge.append(genome)
        genome.fitness = 0
        fitnesses.append(genome.fitness)
        net = neat.nn.FeedForwardNetwork.create(genome, config)
        nets.append(net)

    
    for i in range(len(players)):
        print(i)
        try:
            for p in range(10):
                
                    #if i==0 and p==0:
                        #players[i].message(str(gen))
                    # geting blocks in a 5 block radius
                    blocksd=players[i].getblocks(3)
                    #inv=players[i].getitem()
                    #itemIds=inv.IDs
                    #itemNames=inv.Names
                    blocks = blocksd.IDs
                    blocknames = blocksd.Names

                    
                    outputs = nets[i].activate(tuple(blocks) + players[i].getPos()+(p,))
                    
                    #GUIi=currntplayer
                    
                    #players[i].move(0,abs(outputs[0]), 0)
                    players[i].move(outputs[1],0,outputs[2])
                    
                    location=listbin(outputs[3:10])
                    l=0
                    #print(location,location<=125)
                    if location<=125:
                        for x in range(-2,2):
                            for y in range(-2,2):
                                for z in range(-2,2):
                                    if l==location:
                                        break
                                    l+=1
                        players[i].block(x,y,z)
                    
                    
                        
                        
                    

                    # reward system


                    istherealog = block in blocknames
                    
                
                    if istherealog:
                        ge[i].fitness += 10
                        fitnesses[i] += 10
                        
                        #print(i, "+10")
                        
                    else:
                        ge[i].fitness -= 10
                        fitnesses[i] -= 10
                        
                        #print(i, "-10")
        except:
            pass
            
    gen+=1
        
    
    

def replay_genome(config_path, genome_path="winner.pkl"):
    # Load requried NEAT config
    config = neat.config.Config(neat.DefaultGenome, neat.DefaultReproduction, neat.DefaultSpeciesSet,
                                neat.DefaultStagnation, config_path)

    # Unpickle saved winner
    with open(genome_path, "rb") as f:
        genome = pickle.load(f)

    # Convert loaded genome into required data structure
    genomes = [(1, genome)]

    # Call game with only the loaded genome
    neat.game(genomes, config)

def run(config_path):
    config = neat.config.Config(neat.genome.DefaultGenome, neat.DefaultReproduction,
                                neat.DefaultSpeciesSet, neat.DefaultStagnation,
                                config_path)
    pop = neat.Population(config)
    stats=neat.StatisticsReporter()
    
    for i in range(60):
        bot=Bot(18, 64, 15)
        players.append(bot)
    pop.add_reporter(stats)
    pop.add_reporter(neat.StdOutReporter(True))
    pop.add_reporter(neat.Checkpointer(5))
    winner = pop.run(eval_genomes, 10000)
    with open("winner.pkl", "wb") as f:
        pickle.dump(winner, f)
        f.close()
    print("done outputed file")






print("staring neat...")


local_dir = os.path.dirname(__file__)
config_path = os.path.join(local_dir, 'ai.txt')
run(config_path)
