from controller.models import Route, RouteComponent, Courier
import controller.Routetest as rt
from controller.breakuproute import break_up_route
def assign_route(courier_id, start_point, end_point):
    courier = Courier.objects.filter(id=courier_id)[0]
    old_route = Route.objects.filter(courier=courier).delete()
    route = rt.makeroute([start_point, end_point], rt.key, rt.maxtraveltime)
    sections = break_up_route(route, 36000000)
    routeDB = Route(courier=courier, length= len(sections), current=0)
    routeDB.save()
    full = RouteComponent(route=routeDB, json=route, position=-1)
    full.save()
    for i in range(len(sections)):
        component = RouteComponent(route=routeDB, json=sections[i], position=i)
        component.save()

