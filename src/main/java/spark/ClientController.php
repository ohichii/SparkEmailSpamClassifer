<?php

namespace AppBundle\Controller;

use Sensio\Bundle\FrameworkExtraBundle\Configuration\Route;
use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use AppBundle\Entity\Client;
use AppBundle\Form\ClientType;
use Doctrine\ORM\EntityManagerInterface;

class ClientController extends Controller
{
  /**
   * @Route("/addclient", name="addclient")
   */
  public function addClientAction(Request $request)
  {
      $client = new Client();
      $form = $this->createForm(ClientType::class, $client);

      $form->handleRequest($request);

      if ($form->isSubmitted() && $form->isValid()) {
          // $form->getData() holds the submitted values
          $client = $form->getData();

          // perform some action, such as saving the client to the database
          // for example, if Task is a Doctrine entity, save it!
          $em = $this->getDoctrine()->getManager();
          $em->persist($client);
          $em->flush();
          $clientId = $client->getIdClient();

          return $this->redirectToRoute('addproduct', array(
          'clientId' => $clientId
            ));
      }

      return $this->render('default/client.html.twig', array(
          'form' => $form->createView(),
      ));
   }
}
